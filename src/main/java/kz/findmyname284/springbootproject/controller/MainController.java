package kz.findmyname284.springbootproject.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import kz.findmyname284.springbootproject.enums.OrderStatus;
import kz.findmyname284.springbootproject.enums.UserRole;
import kz.findmyname284.springbootproject.exception.AuthorizationException;
import kz.findmyname284.springbootproject.model.CatalogProduct;
import kz.findmyname284.springbootproject.model.Employee;
import kz.findmyname284.springbootproject.model.Order;
import kz.findmyname284.springbootproject.model.Supplier;
import kz.findmyname284.springbootproject.model.User;
import kz.findmyname284.springbootproject.model.WarehouseProduct;
import kz.findmyname284.springbootproject.repository.CatalogProductRepository;
import kz.findmyname284.springbootproject.repository.WarehouseProductRepository;
import kz.findmyname284.springbootproject.service.EmployeeService;
import kz.findmyname284.springbootproject.service.OrderService;
import kz.findmyname284.springbootproject.service.SupplierService;
import kz.findmyname284.springbootproject.service.UserService;
import kz.findmyname284.springbootproject.utils.Authorization;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class MainController {
	private final UserService userService;
	private final OrderService orderService;
	private final SupplierService supplierService;
	private final EmployeeService employeeService;
	private final WarehouseProductRepository wProductRepository;
	private final CatalogProductRepository cProductRepository;

	@GetMapping("/")
	public String root(Model model) {
		model.addAttribute("title", "Main Page");
		return "index";
	}

	@GetMapping("home")
	public String hello(Model model, @AuthenticationPrincipal UserDetails userDetails) {
		if (userDetails != null) {
			User user = userService.findByUsername(userDetails.getUsername());
			model.addAttribute("user", user);
			model.addAttribute("orders", orderService.findByUser(user).size());
		}
		model.addAttribute("title", "Home Page");
		model.addAttribute("activePage", "home");
		return "home";
	}

	@GetMapping("auth")
	public String auth(Model model, @AuthenticationPrincipal UserDetails userDetails) {
		if (userDetails != null) {
			return "redirect:/home";
		}
		model.addAttribute("title", "Auth Page");
		return "auth";
	}

	@GetMapping("cart")
	public String cart(Model model) {
		model.addAttribute("title", "Cart Page");
		return "cart/view";
	}

	@GetMapping("cart/checkout")
	public String checkout(@RequestParam(name = "orderId", required = false) Long orderId, Model model,
			@AuthenticationPrincipal UserDetails userDetails,
			RedirectAttributes redirectAttributes) {
		try {
			User user = Authorization.getAuthorizationUser(userService, userDetails);
			Order order = orderService.findById(orderId);

			if (!order.getUser().getId().equals(user.getId())) {
				redirectAttributes.addFlashAttribute("error", "Access denied");
				return "redirect:/cart/checkout/status/" + order.getId();
			}

			if (order.getItems().isEmpty()) {
				throw new IllegalArgumentException("Order is empty");
			}

			if (order.getStatus() != OrderStatus.PENDING) {
				return "redirect:/cart/checkout/status/" + order.getId();
			}

			model.addAttribute("order", order);
			model.addAttribute("user", user);
			model.addAttribute("title", "Оформление заказа #" + order.getId());

			BigDecimal total = order.getItems().stream()
					.map(item -> item.getProduct().getPrice().multiply(BigDecimal.ONE.subtract(item.getProduct().getDiscount().divide(BigDecimal.valueOf(100))))
							.multiply(BigDecimal.valueOf(item.getQuantity())))
					.reduce(BigDecimal.ZERO, BigDecimal::add);
			model.addAttribute("totalAmount", total.toString());

		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
			return "redirect:/cart";
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Access denied");
			return "redirect:/login";
		}

		return "cart/checkout";
	}

	@GetMapping("/cart/checkout/status/{orderId}")
	public String orderStatus(@PathVariable Long orderId, Model model,
			RedirectAttributes redirectAttributes) {
		try {
			Order order = orderService.findById(orderId);

			if (order == null) {
				redirectAttributes.addFlashAttribute("error", "Order not found");
				return "redirect:/cart";
			}

			model.addAttribute("order", order);
			model.addAttribute("title", "Order #" + order.getId());
			BigDecimal total = order.getItems().stream()
					.map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
					.reduce(BigDecimal.ZERO, BigDecimal::add);
			model.addAttribute("totalAmount", total.toString());
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
			return "redirect:/login";
		}

		return "cart/status";
	}

	@GetMapping("getAdmin")
	public String getAdmin(@AuthenticationPrincipal UserDetails userDetails) {
		try {
			User user = Authorization.getAuthorizationUser(userService, userDetails);
			user.setRole(UserRole.ADMIN);
			userService.save(user);

			return "redirect:/admin";
		} catch (Exception e) {
			return "redirect:/";
		}
	}

	@GetMapping("/dashboard")
	public String dashboard(Model model,
			@AuthenticationPrincipal UserDetails userDetails) {
		try {

			User user = Authorization.getAuthorizationUser(userService, userDetails);

			Supplier supplier = supplierService.findByUser(user);
			Optional<Employee> OptionalEmployee = employeeService.findByUser(user);
			List<Order> orders = orderService.findByUser(user);
			List<CatalogProduct> supplierProducts = cProductRepository.findAllBySeller(supplier);
			List<WarehouseProduct> products = wProductRepository.findAll();

			if (OptionalEmployee.isPresent()) {
				Employee employee = OptionalEmployee.get();

				model.addAttribute("employee", employee);
				model.addAttribute("products", products);
			} else if (supplier != null) {
				model.addAttribute("supplier", supplier);
				model.addAttribute("supplierProducts", supplierProducts);
				products = products.stream()
						.filter(p -> p.getBasePrice() != null)
						.collect(Collectors.toList());
				model.addAttribute("products", products);
			}

			model.addAttribute("user", user);
			model.addAttribute("title", "Dashboard Page");
			model.addAttribute("activeTab", "profile");

			BigDecimal totalAmount = orders.stream()
					.flatMap(order -> order.getItems().stream())
					.filter(item -> item.getOrder().getStatus() == OrderStatus.DELIVERED)
					.map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
					.reduce(BigDecimal.ZERO, BigDecimal::add);
			model.addAttribute("totalAmount", totalAmount);
			model.addAttribute("orders", orders);

			return "profile/dashboard";

		} catch (AuthorizationException e) {
			return "redirect:/auth";
		}
	}

	@GetMapping("catalog")
	public String catalog(Model model) {
		model.addAttribute("title", "Catalog Page");

		List<CatalogProduct> products = cProductRepository.findAll();

		model.addAttribute("products", products);
		model.addAttribute("activePage", "catalog");
		return "products/catalog";
	}

	@GetMapping("/returns")
	public String allReturns(Model model, @AuthenticationPrincipal UserDetails userDetails) {
		try {
			User user = Authorization.getAuthorizationUser(userService, userDetails);
			List<Order> orders = orderService.findByUser(user);
			model.addAttribute("returns", orders);
			model.addAttribute("title", "Returns Page");
			model.addAttribute("activePage", "returns");
		} catch (AuthorizationException e) {
			return "redirect:/auth";
		}
		return "cart/returns";
	}

	@GetMapping("/returns/{returnId}")
	public String returnDetails(@PathVariable Long returnId,
			@AuthenticationPrincipal UserDetails userDetails,
			Model model,
			RedirectAttributes redirectAttributes) {
		try {
			User user = Authorization.getAuthorizationUser(userService, userDetails);
			Order order = orderService.findById(returnId);
			if (order.getUser().equals(user)) {
				model.addAttribute("return", order);
				model.addAttribute("title", "Return Page");
				model.addAttribute("id", returnId);
				return "cart/return";
			}
			return "cart/auth";
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Return request not found");
			return "redirect:/returns";
		}
	}

	@GetMapping("/balance")
	public String addBalance(Model model, @AuthenticationPrincipal UserDetails userDetails) {
		try {
			User user = Authorization.getAuthorizationUser(userService, userDetails);
			model.addAttribute("user", user);
			model.addAttribute("title", "Balance Page");
			model.addAttribute("activePage", "balance");
			return "profile/balance";
		} catch (AuthorizationException e) {
			return "redirect:/auth";
		}
	}
}
