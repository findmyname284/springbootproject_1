package kz.findmyname284.springbootproject.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import kz.findmyname284.springbootproject.enums.OrderStatus;
import kz.findmyname284.springbootproject.model.Order;
import kz.findmyname284.springbootproject.model.Product;
import kz.findmyname284.springbootproject.model.Supplier;
import kz.findmyname284.springbootproject.model.User;
import kz.findmyname284.springbootproject.service.OrderService;
import kz.findmyname284.springbootproject.service.ProductService;
import kz.findmyname284.springbootproject.service.SupplierService;
import kz.findmyname284.springbootproject.service.UserService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class MainController {

	private final UserService userService;
	private final OrderService orderService;
	private final SupplierService supplierService;
	private final ProductService productService;

	@GetMapping("/")
	public String root(Model model) {
		model.addAttribute("title", "Main Page");
		return "index";
	}

	@GetMapping("home")
	public String hello(Model model) {
		model.addAttribute("title", "Home Page");
		return "home";
	}

	@GetMapping("auth")
	public String auth(Model model) {
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
		if (userDetails == null) {
			return "redirect:/auth";
		}
		Optional<User> userOptional = userService.findByUsername(userDetails.getUsername());

		if (!userOptional.isPresent()) {
			return "redirect:/auth";
		}

		User user = userOptional.get();
		try {
			Order order = orderService.findById(orderId);

			if (!order.getUser().getId().equals(user.getId())) {
				redirectAttributes.addFlashAttribute("error", "Access denied");
				return "redirect:/cart";
			}

			if (order.getItems().isEmpty()) {
				throw new IllegalArgumentException("Order is empty");
			}

			model.addAttribute("order", order);
			model.addAttribute("user", user);
			model.addAttribute("title", "Оформление заказа #" + order.getId());

			BigDecimal total = order.getItems().stream()
					.map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
					.reduce(BigDecimal.ZERO, BigDecimal::add);
			model.addAttribute("totalAmount", total.toString());

		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
			return "redirect:/cart";
		}

		return "cart/checkout";
	}

	@GetMapping("/dashboard")
	public String dashboard(Model model,
			@AuthenticationPrincipal UserDetails userDetails) {
		if (userDetails == null) {
			return "redirect:/auth";
		}
		Optional<User> userOptional = userService.findByUsername(userDetails.getUsername());

		if (!userOptional.isPresent()) {
			return "redirect:/auth";
		}

		User user = userOptional.get();
		Supplier supplier = supplierService.findByUser(user);
		List<Order> orders = orderService.findByUser(user);
		List<Product> supplierProducts = productService.findBySupplier(supplier);

		model.addAttribute("user", user);
		model.addAttribute("title", "Dashboard Page");
		model.addAttribute("activeTab", "profile");
		model.addAttribute("supplier", supplier);
		model.addAttribute("supplierProducts", supplierProducts);
		BigDecimal total = orders.stream()
				.flatMap(order -> order.getItems().stream())
				.filter(item -> item.getOrder().getStatus() != OrderStatus.PENDING)
				.map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		model.addAttribute("totalAmount", total.toString());
		model.addAttribute("orders", orders);

		return "profile/dashboard";
	}

	@GetMapping("catalog")
	public String catalog(Model model) {
		model.addAttribute("title", "Catalog Page");

		List<Product> products = productService.findAll();

		model.addAttribute("products", products);
		model.addAttribute("activePage", "catalog");
		return "products/catalog";
	}

}
