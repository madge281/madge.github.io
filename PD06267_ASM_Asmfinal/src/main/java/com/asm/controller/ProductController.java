package com.asm.controller;

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

import com.asm.bean.Category;
import com.asm.bean.Product;
import com.asm.bean.ProductCategory;
import com.asm.service.BrandService;
import com.asm.service.CategoryService;
import com.asm.service.ProductService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@RequestMapping("/product")
public class ProductController {
	@Autowired BrandService bService;
	@Autowired CategoryService cService;
	@Autowired ProductService pService;
	
	@RequestMapping("/list")
	public String home(Model model, // model để truyền dữ liệu
			@RequestParam("kw") Optional<String> kw, //Tham số yêu cầu (kw) để tìm kiếm sản phẩm theo từ khóa
			@RequestParam("cid") Optional<String> cid, // Tham số danh mục (cid) để tìm kiếm sản phẩm theo danh mục
			@RequestParam("bid") Optional<String> bid, // Tham số thương hiệu (bid) để tìm kiếm sản phẩm theo thương hiệu
			@RequestParam("p") Optional<Integer> p) { // Tham số trang (p) để xác định trang hiện tại của kết quả tìm kiếm sản phẩm
			
			/*
			* Kiểm tra tham số cid có được cung cấp hay không. Nếu có, tìm kiếm sản phẩm theo danh mục gọi phương thức pService.findProductByCategory(cid, p). 
			* và gán kết quả tìm kiếm cho lstProduct và danh sách sản phẩm cho products trong đối tượng model
			*/
		if(cid.isPresent()) {
				Page<Product> lstProduct = pService.findProductByCategory(cid, p);
				List<Map<String, Object>> products = pService.listProductSearch(lstProduct);
				model.addAttribute("page", lstProduct);
				model.addAttribute("products", products);
	
	//Kiểm tra tham số bid có được cung cấp hay không. Nếu có, thực hiện tìm kiếm sản phẩm theo danh mục gọi phương thức pService.findProductByCategory(cid, p). 
			}else if(bid.isPresent()) {
				Page<Product> lstProduct = pService.findProductByBrand(bid, p);
				List<Map<String, Object>> products = pService.listProductSearch(lstProduct);
				model.addAttribute("page", lstProduct); 
				model.addAttribute("products", products); 
				
			// nếu cid và bid kh đc cung cấp, gọi phương thức pService.searchProductByName(kw, p) để tìm kiếm sản phẩm theo từ khóa 
			}else {
				Page<Product> lstProduct = pService.searchProductByName(kw, p);
				List<Map<String, Object>> products = pService.listProductSearch(lstProduct);
				model.addAttribute("page", lstProduct); 
				model.addAttribute("products", products);
			}
		return "product/list";
	}
	
	@GetMapping("/list/brand")
	public String filterByListBrand(Model model, // đối tượng model để truyền dữ liệu đến view
			@RequestParam("bid") List<String> bid, // tham số bid là danh sách các chuỗi, đại diện cho các brand
			@RequestParam("p") Optional<Integer> p) { // tham số p là 1 số nguyên tùy chọn, đại diện cho trang hiện tại
		//Gọi phương thức findProductByListBrand(bid, p) của pService để tìm các sản phẩm dựa trên danh sách các thương hiệu.
		Page<Product> lstProduct = pService.findProductByListBrand(bid, p);
		List<Map<String, Object>> products = pService.listProductSearch(lstProduct);
		model.addAttribute("page", lstProduct); // thuộc tính page trong model là page chứa danh sách các sản phẩm
		model.addAttribute("products", products); // thuộc tính products trong model là danh sách chứa các thông tin sản phẩm
		return "product/list";
	}
	
	//lọc danh sách sản phẩm theo mức giá và trả về kết quả để hiển thị trên trang "product/list".
	@GetMapping("/list/price/{price}")
	public String filterByPrice(Model model,
			@PathVariable("price") String price, // tham số price đại diện cho mức giá được chọn
			@RequestParam("p") Optional<Integer> p) { // tham số p đại diện cho trang hiện tại
		if(price.equalsIgnoreCase("under100")) { // nếu giá nhỏ hơn 100, lấy danh sách các sản phẩm có giá nhỏ hơn 100.000 đồng.
			Page<Product> lstProduct = pService.findProductLessThanPrice(100000.0, p);
			List<Map<String, Object>> products = pService.listProductSearch(lstProduct);
			model.addAttribute("page",lstProduct);
			model.addAttribute("products", products);
		}
		else if(price.equalsIgnoreCase("100-300")) {
			Page<Product> lstProduct = pService.findProductBetweenPrice(100000.0, 300000.0, p);
			List<Map<String, Object>> products = pService.listProductSearch(lstProduct);
			model.addAttribute("page",lstProduct);
			model.addAttribute("products", products);
		}
		else if(price.equalsIgnoreCase("300-900")) {
			Page<Product> lstProduct = pService.findProductBetweenPrice(300000.0, 900000.0, p);
			List<Map<String, Object>> products = pService.listProductSearch(lstProduct);
			model.addAttribute("page",lstProduct);
			model.addAttribute("products", products);
		}
		else if(price.equalsIgnoreCase("over900")) {
			Page<Product> lstProduct = pService.findByPriceGreaterThanEqual(900000.0, p);
			List<Map<String, Object>> products = pService.listProductSearch(lstProduct);
			model.addAttribute("page",lstProduct);
			model.addAttribute("products", products);
		}
		return "product/list";
	}
	
	// lấy thông tin chi tiết sản phẩm, danh sách các sản phẩm tương tự và trả về kết quả để hiển thị trên trang "product/product-detail"
	@RequestMapping("/detail/{id}")
	public String detail(Model model,
			@PathVariable("id") Long id) { // tham số id sản phẩm
		Map<String, Object> map = pService.ProductDetail(id); // gọi phương thức ProductDetail từ pService để lấy thông tin sản phẩm và lưu vào map
		model.addAttribute("product", map); // Đặt thuộc tính "product" trong model là đối tượng Product lấy từ map.
		
		//Lấy danh sách các ProductCategory của sản phẩm và tạo danh sách các id của các category.
		Product product = (Product) map.get("product");
		List<ProductCategory> productCates = product.getProductCategories();
		List<String> categories = new ArrayList<String>();
		for (ProductCategory productCate : productCates){
			categories.add(productCate.getCategory().getId());
		}
		
		Optional<Integer> p = Optional.of(0); // tạo 1 lớp Optional Integer với giá trị = 0 đại diện cho trang hiện tại
		//Gọi phương thức findProductByListCategory(categories, p) từ service pService để lấy danh sách các sản phẩm thuộc các category tương tự. 
		//Kết quả được lưu vào đối tượng Page<Product> có tên là pageProduct.
		Page<Product> pageProduct = pService.findProductByListCategory(categories, p);
		List<Map<String, Object>> products = pService.listProductSearch(pageProduct);
		model.addAttribute("productsRcm", products);
		return "product/product-detail";
	}
}
