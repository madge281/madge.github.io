package com.asm.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asm.bean.Product;
import com.asm.service.ProductService;

@RestController
@CrossOrigin("*")
public class CartRestController {
	
	@Autowired ProductService pService;
	
	//gọi và trả về thông tin của sản phẩm tương ứng.
	@RequestMapping("/rest/products/{id}")
	//Nhận vào id của sản phẩm cần lấy thông tin thông qua @PathVariable("id").
	public Product getOne(@PathVariable("id") Long id) {
		//Gọi phương thức findById với id của sản phẩm để tìm kiếm và trả về đối tượng Product.
		return pService.findById(id);
	}
}
