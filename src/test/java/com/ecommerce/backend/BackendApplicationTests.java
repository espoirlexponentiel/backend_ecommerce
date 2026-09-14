package com.ecommerce.backend;

import com.ecommerce.backend.controller.OrderController;
import com.ecommerce.backend.entity.Order;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.repository.UserRepository;
import com.ecommerce.backend.security.JwtUtil;
import com.ecommerce.backend.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BackendApplicationTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderController orderController;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testOrderRetrievalForUser() throws Exception {
        User user = userRepository.findByEmail("espoirchakpali15@gmail.com").orElse(null);
        System.out.println("TEST USER FOUND: " + user);
        if (user != null) {
            List<Order> orders = orderService.getOrdersByUser(user);
            System.out.println("ORDERS FOUND BY SERVICE: " + orders.size());
            for (Order o : orders) {
                System.out.println("ORDER ID: " + o.getId() + ", TOTAL: " + o.getTotalAmount() + ", ITEMS: " + (o.getItems() != null ? o.getItems().size() : 0));
            }

            ResponseEntity<?> response = orderController.getUserOrders(user);
            System.out.println("CONTROLLER RESPONSE STATUS: " + response.getStatusCode());
            System.out.println("CONTROLLER RESPONSE BODY: " + response.getBody());

            String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
            System.out.println("GENERATED TOKEN: " + token);

            mockMvc.perform(get("/api/orders")
                    .header("Authorization", "Bearer " + token))
                    .andDo(result -> {
                        System.out.println("MOCKMVC STATUS: " + result.getResponse().getStatus());
                        System.out.println("MOCKMVC CONTENT: " + result.getResponse().getContentAsString());
                    })
                    .andExpect(status().isOk());
        }
    }
}

