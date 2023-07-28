package com.asm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.asm.bean.Account;
import com.asm.bean.Role;
import com.asm.bean.RoleDetail;
import com.asm.service.AccountService;
import com.asm.service.BrandService;
import com.asm.service.CategoryService;
import com.asm.service.MailerService;
import com.asm.service.ProductService;
import com.asm.service.SessionService;

@Controller
public class HomeController {
	@Autowired
	BrandService bService;
	@Autowired
	CategoryService cService;
	@Autowired
	ProductService pService;
	@Autowired
	SessionService session;
	@Autowired
	AccountService aService;
	@Autowired 
	MailerService mailer;

	@RequestMapping("/admin")
	public String admin() {	
		return "admin/index";
	}

	//thực hiện việc lấy danh sách sản phẩm theo ngày tạo và trả về kết quả để hiển thị trên trang chủ.
	@RequestMapping("/")
	public String home(Model model) {
		// load ds product xep theo ngay tao
		model.addAttribute("db", pService.findProductByCreateDateDESC()); //lấy danh sách sản phẩm từ service và đưa vào model để hiển thị trên trang chủ.
		return "home/index";
	}
	
	//hiển thị trang danh sách các brand
	@GetMapping("/brand/list")
	public String brandList(Model model) { // truyền dữ liệu controller đến view
		return "brand/list";
	}
	
	@GetMapping("/register")
	public String register(	@ModelAttribute Account account) { // @ModelAttribute để truyền đối tượng Account từ controller đến view
		return "register";
	}
	
	// đky account
	@PostMapping("/register")
	public String signup(Model model,
			@ModelAttribute Account account) {
		if(aService.existsById(account.getUsername())) { // Kiểm tra xem tài khoản đã tồn tại hay chưa
			model.addAttribute("error", "Đã tồn tại username "+account.getUsername());
			return "register";
		}else {
			account.setActivated(true); // account đc kích hoạt
			account.setPhoto("logo.jpg"); // thiết lập ảnh đại diện cho tài khoản
			
			// xem quyền truy cập cho account mới tạo
			Role r = new Role();
			r.setRole("user");
			RoleDetail rd = new RoleDetail();
			rd.setAccount(account);
			rd.setRole(r);
			
			// lưu thông tin vào db
			aService.save(account);
			aService.saveRoleDetail(rd);
			return "redirect:/register/success";
		}
	}
	
	// hiển thị thông báo sau khi đky thành công
	@RequestMapping("/register/success")
	public String registerSuccess(Model model) { 	
		model.addAttribute("message", "Đăng ký thành công"); 
		return "login";
	}

	//thông báo lỗi hoặc thông báo thành công sau khi đăng ký.
	@GetMapping("/login")
	public String formLogin(Model model, @RequestParam(value = "message", required = false) String message) {
		model.addAttribute("message", message);
		return "login";
	}
	
	//kiểm tra tên đăng nhập và mật khẩu nhập vào và xử lý quá trình đăng nhập và truyền thông điệp tương ứng vào model để hiển thị cho người dùng.
	@PostMapping("/login")
	public String login(@RequestParam("username") String username,
			@RequestParam("password") String password, 
			Model model) {
		try {	
			Account account = aService.findByUsername(username); // tìm account trong db bằng cách gọi phương thức findByUsername
			if(!account.getPassword().equals(password)) { 
				model.addAttribute("message", "Invalid password");
			}else {
				String uri = session.get("security-uri");

					session.set("user", account); // lưu thông tin người dùng đăng nhậ[ trong session với key "user"
					
					if(this.checkAdmin(account)) {
						session.set("userAdmin", "admin"); // lưu thông tin "admin" vào session thông qua session.set("userAdmin", "admin").
					}
					model.addAttribute("message", "Login success"); 
			}
		} catch (Exception e) {
			model.addAttribute("message", "Invalid username");
		}
		return "login";
	}
	
	// kiểm tra từng vai trò của tài khoản và trả về giá trị true nếu tài khoản có vai trò là "staff" hoặc "director", tức là có quyền admin. 
	// Nếu không, trả về giá trị false.
	public Boolean checkAdmin(Account account) {
		for(RoleDetail roleDetail : account.getRoleDetails()) {
			//check xem vai trò là "staff" hay "director" và tài khoản có quyền admin và phương thức trả về giá trị true.
			if(roleDetail.getRole().getRole().equals("staff") || roleDetail.getRole().getRole().equals("director")) { 
				return true;
			}
		}
		// return về fasle => account kh có quyền admin
		return false;
	}
	
	@RequestMapping("/logout")
	public String logoutSuccess(Model model) {
		// gỡ bỏ các khóa user, userAdmin, security-uri,uri để đăng xuất
		session.remove("user");
		session.remove("userAdmin");
		session.remove("security-uri");
		session.remove("uri");
		model.addAttribute("message", "Đăng xuất thành công");
		return "login";
	}
	
	@GetMapping("forgot-password")
	public String forgot() {
		return "forgot";
	}
		
		@PostMapping("forgot-password")
		public String forgot(@RequestParam("username") String username, Model model) {
			try {
				Account account = aService.findByUsername(username); // tìm tài khoản dựa trên username
				String to = account.getEmail();	
				String email = to.substring(0, 2);
				
				double randomDouble = Math.random();
	            randomDouble = randomDouble * 1000000 + 1;
	            int randomInt = (int) randomDouble;
				
				String subject = "Lấy lại mật khẩu";
				String body = "Mật khẩu của bạn là:"+randomInt;
				mailer.send(to, subject, body);
				
				account.setPassword(String.valueOf(randomInt));
				aService.save(account);
				
				model.addAttribute("message", "Mật khẩu mới đã được gửi đến mail "+email+"***");
			} catch (Exception e) {
				model.addAttribute("message", "Invalid Username");
			}
			return "forgot";
		}
	}
