package kz.findmyname284.springbootproject.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import kz.findmyname284.springbootproject.enums.UserRole;
import kz.findmyname284.springbootproject.model.Order;
import kz.findmyname284.springbootproject.model.User;
import kz.findmyname284.springbootproject.model.WarehouseProduct;
import kz.findmyname284.springbootproject.repository.WarehouseProductRepository;
import kz.findmyname284.springbootproject.service.OrderService;
import kz.findmyname284.springbootproject.service.UserService;
import kz.findmyname284.springbootproject.utils.Authorization;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
	private final UserService userService;
    private final OrderService orderService;
    private final WarehouseProductRepository wProductRepository;

    @GetMapping()
    public String users(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = Authorization.getAuthorizationUser(userService, userDetails);

            Authorization.checkRole(user, UserRole.ADMIN);

            List<WarehouseProduct> products = wProductRepository.findAll();
            List<Order> orders = orderService.findAll();

            model.addAttribute("products", products);

            model.addAttribute("title", "Страница администратора");
            model.addAttribute("orders", orders);
            model.addAttribute("isManager", true);
            return "admin/index";
        } catch (Exception e) {
            return "redirect:/";
        }
    }

}
