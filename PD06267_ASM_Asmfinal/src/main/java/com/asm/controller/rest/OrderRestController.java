package com.asm.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.asm.bean.Order;
import com.asm.service.OrderService;
import com.fasterxml.jackson.databind.JsonNode;

@CrossOrigin("*") //cho phép yêu cầu từ bất kỳ nguồn nào được gửi đến API của bạn
@RestController
public class OrderRestController {
	@Autowired OrderService oService;
	
	@PostMapping("/rest/order")
	//xử lý thông tin đơn hàng và trả về đối tượng Order đã được tạo.
	public Order create(@RequestBody JsonNode order) {
		return oService.create(order);
	}
}
