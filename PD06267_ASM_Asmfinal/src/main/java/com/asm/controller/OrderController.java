package com.asm.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.asm.bean.Account;
import com.asm.bean.Category;
import com.asm.bean.Order;
import com.asm.bean.OrderDetail;
import com.asm.bean.Product;
import com.asm.dao.OrderDetailRepo;
import com.asm.service.AccountService;
import com.asm.service.BrandService;
import com.asm.service.CategoryService;
import com.asm.service.OrderService;
import com.asm.service.ProductService;
import com.asm.service.SessionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@RequestMapping("/order")
public class OrderController {
	@Autowired BrandService bService;
	@Autowired CategoryService cService;
	@Autowired ProductService pService;
	@Autowired AccountService aService;
	@Autowired OrderService oService;
	@Autowired SessionService session;
	
	
	// lấy danh sách đơn hàng của người dùng hiện tại, tính tổng giá trị của mỗi đơn hàng và truyền danh sách này vào giao diện người dùng để hiển thị
	@RequestMapping("/list")
	public String listOrder(Model model) {
		//Lấy thông tin người dùng hiện tại từ đối tượng session (session.get("user")) và gán cho biến account.
		Account account = session.get("user");
		
		// Gọi phương thức findByUsername từ OrderService để tìm kiếm các đơn hàng của người dùng
		List<Order> orders = oService.findByUsername(account.getUsername());

		// Tạo một danh sách db  để lưu trữ thông tin về đơn hàng và tổng giá trị của mỗi đơn hàng.
		List<Map<String, Object>> db = new ArrayList<Map<String,Object>>();
		
		// dùng vòng lặp for để duyệt qua từng đơn hàng trong danh sách orders.
		for(Order order : orders ) {
			// Trong mỗi vòng lặp, tạo một đối tượng Map<String, Object> để lưu trữ thông tin về đơn hàng và tổng giá trị của đơn hàng.
			Map<String, Object> map = new HashMap<String, Object>();
			// lấy ds đơn hàng và tính tổng đơn hàng
			List<OrderDetail> orderDetail = order.getOrderDetails();
			Double total = (double) 0;
			for(OrderDetail od : orderDetail) {
				total += od.getPrice() * od.getQuantity();
			}
			//Đưa thông tin đơn hàng  và tổng giá vào map và thêm map vào danh sách db.
			map.put("order", order);		
			map.put("total", total);
			db.add(map);
		}
		//Truyền danh sách đơn hàng (db) vào đối tượng model 
		model.addAttribute("orders", db);
		return "order/list";
	}
	
	@RequestMapping("/cart")
	public String cart() {
		return "order/cart";
	}
	// phương thức checkout được sử dụng để hiển thị trang thanh toán và truyền thông tin chi tiết người dùng cho trang đó. 
	@RequestMapping("/checkout")
	public String checkout(Model model) {
		session.get("user"); // lấy thông tin chi tiết người dùng từ đối tượng session.
		model.addAttribute("userDetail", session.get("user")); 		
		return "order/checkout";
	}
	
	@RequestMapping("/detail/{id}")
	public String detail(Model model, 
			Principal principal,
			@PathVariable("id") Long id) {
		Order order = oService.findById(id); // phương thức findById(id) để lấy thông tin về đơn hàng dựa trên id
		Account account = session.get("user"); // session.get("user") để lấy thông tin chi tiết người dùng
		String acc = account.getUsername();
		if(!order.getAccount().getUsername().equals(acc)) {	// ktra người dùng có quyền đăng nhập vào đơn hàng
			return "redirect:/login?message=Access%20Denied"; // truy cập bị từ chối
		}else {
			model.addAttribute("order", order); // gán thuộc tính order trong đối tượng model
			//Tính tổng tiền của đơn hàng bằng cách duyệt qua danh sách orderDetail
			List<OrderDetail> orderDetail = order.getOrderDetails();  
			Double total = (double) 0;
			for(OrderDetail od : orderDetail) {
				total += od.getPrice() * od.getQuantity();
			}
			model.addAttribute("total", total); 
			return "order/detail";
			
		}
	}
}
