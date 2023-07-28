package com.asm.controller.rest;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asm.bean.Order;
import com.asm.bean.OrderDetail;
import com.asm.report.ReportCategory;
import com.asm.report.ReportCost;
import com.asm.report.ReportProduct;
import com.asm.service.AccountService;
import com.asm.service.OrderService;
import com.asm.service.ReportService;

@RestController
@CrossOrigin("*")
@RequestMapping("/admin/rest/report")
public class ReportRestController {
	@Autowired AccountService aService;
	@Autowired OrderService oService;
	@Autowired ReportService rpService;
	
	public Integer monthCurrent() {
		Date date = new Date();
		/*
		 * date.getMonth() được gọi để lấy chỉ số của tháng trong phạm vi từ 0 đến 11, với 0 là tháng 1 và 11 là tháng 12. 
		 * Để trả về số tháng hiện tại từ 1 đến 12, ta cộng thêm 1 vào kết quả (date.getMonth()+1).
		 */
		return date.getMonth()+1;
	}
	
	@GetMapping("/total")
	public Map<String, Object> total() {
		Integer month = this.monthCurrent(); // monthCurrent() để lấy tháng hiện tại
		Map<String, Object> db = new HashMap<String, Object>(); // HashMap được khởi tạo với tên biến là db để chứa các thông tin tổng hợp
		/*
		 * db.put("totalCustomer", aService.countCustomer("user")) được sử dụng để đặt số lượng khách hàng vào db
		 * aService.countCustomer("user") được gọi để đếm số lượng khách hàng có quyền "user" trong hệ thống
		 */
		db.put("totalCustomer", aService.countCustomer("user"));
		List<Order> orders = oService.findOrderInMonth(month); // lấy danh sách đơn hàng trong tháng hiện tại
		Double totalCost = 0.0; // tính tổng danh thu
		// vòng lặp for với biến order,duyệt từng đơn hàng và danh sách orderDetail của đơn hàng đó được lấy ra
		for(Order order : orders ) {	
			List<OrderDetail> orderDetail = order.getOrderDetails();
	//vòng lặp for tiếp theo với biến od, giá trị od.getPrice() nhân với od.getQuantity() được cộng vào totalCost để tính tổng doanh thu
			for(OrderDetail od : orderDetail) {
				totalCost += od.getPrice() * od.getQuantity();
			}
		}
	// đặt giá trị tổng doanh thu vào db
		db.put("totalCost", totalCost);
	// đặt số lượng đơn hàng trong tháng hiện tại vào db
		db.put("totalOrder", oService.countOrderInMonth(month));
		return db;
	}
	
	@GetMapping("/reportcost")
	/*
	 * Phương thức reportCostInMonth() trả về một danh sách  các đối tượng ReportCost
	 * Đây là danh sách báo cáo chi phí trong tháng hiện tại
	 * Trong phương thức này, this.monthCurrent() được gọi để lấy số tháng hiện tại
		Sau đó, một danh sách lst được khởi tạo.
		lst được gán giá trị từ kết quả của phương thức rpService.reportCostInMonth(this.monthCurrent()) 
		Điều này có nghĩa là phương thức reportCostInMonth() của rpService được gọi với tham số là số tháng hiện tại để trả về danh sách báo cáo chi phí trong tháng đó
		Cuối cùng, danh sách lst được trả về.
	 */
	public List<ReportCost> reportCostInMonth(){
		List<ReportCost> lst = rpService.reportCostInMonth(this.monthCurrent());
		return lst;
	}
	/*
	 * Phương thức reportCostInMonth() của rpService được gọi với tham số là số tháng hiện tại (lấy từ this.monthCurrent()) 
	 * để trả về danh sách báo cáo chi phí trong tháng đó. Sau đó, danh sách lst được trả về từ phương thức đó
	 */
	@GetMapping("/bestSellerInMonth")
	public List<ReportProduct> reportProductInMonth(){
		List<ReportProduct> lst = rpService.reportProductInMonth(this.monthCurrent());
		return lst;
	}
}
