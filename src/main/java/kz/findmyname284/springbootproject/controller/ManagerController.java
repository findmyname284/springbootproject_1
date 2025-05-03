package kz.findmyname284.springbootproject.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import kz.findmyname284.springbootproject.enums.UserRole;
import kz.findmyname284.springbootproject.model.User;
import kz.findmyname284.springbootproject.model.WarehouseProduct;
import kz.findmyname284.springbootproject.repository.WarehouseProductRepository;
import kz.findmyname284.springbootproject.service.UserService;
import kz.findmyname284.springbootproject.utils.Authorization;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/manager")
@RequiredArgsConstructor
public class ManagerController {
    private final WarehouseProductRepository wProductRepository;
    private final UserService userService;

    @GetMapping()
    public String manager(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = Authorization.getAuthorizationUser(userService, userDetails);

            Authorization.checkRole(user, UserRole.MANAGER, UserRole.ADMIN);

            List<WarehouseProduct> products = wProductRepository.findAll();

            model.addAttribute("products", products);
            model.addAttribute("isManager", true);
            model.addAttribute("title", "Manager Page");
            return "manager/index";
        } catch (Exception e) {
            return "redirect:/";
        }
    }

}
