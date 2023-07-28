package com.asm.controller.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.websocket.server.PathParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.asm.bean.Brand;
import com.asm.bean.Product;
import com.asm.bean.ProductCategory;
import com.asm.service.BrandService;
import com.asm.service.CategoryService;
import com.asm.service.ProductService;
import com.asm.service.UploadService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/admin/rest/products")
public class ProductRestController {
	@Autowired
	ProductService pService;
	@Autowired
	BrandService bService;
	@Autowired
	CategoryService cService;

	@Autowired
	UploadService uService;
	
	/*
	 * trả về một đối tượng Map chứa các thông tin về thương hiệu, danh mục, sản phẩm và mục sản phẩm, 
	 * để được sử dụng bởi phía máy khách (client) khi gửi yêu cầu GET đến URL gốc của API.
	 */
	@GetMapping("")
	public Map<String, Object> get() { 
		// Map<String, Object> được tạo ra để lưu trữ các thông tin trả về cho yêu cầu.
		Map<String, Object> map = new HashMap<String, Object>();
		//"brands": Giá trị là danh sách các thương hiệu (Brand) được truy xuất thông qua bService.findAll().
		map.put("brands", bService.findAll()); 
		//"categories": Giá trị là danh sách các danh mục (Category) được truy xuất thông qua cService.findAll().
		map.put("categories", cService.findAll());
		//"products": Giá trị là danh sách các sản phẩm (Product) được truy xuất thông qua pService.findAll()
		map.put("products", pService.findAll());
		//"productCates": Giá trị là danh sách các mục sản phẩm (ProductCategory) được truy xuất thông qua pService.findProductCategory().
		map.put("productCates", pService.findProductCategory());
		return map;
	}

	@GetMapping("/{id}")
	//Tham số {id} được đánh dấu với @PathVariable("id"), cho phép truy cập và sử dụng giá trị của đường dẫn tham số trong phương thức.
	public ResponseEntity<List<ProductCategory>> getProductDetail(@PathVariable("id") Long id) {
		if (!pService.existsById(id)) { //ktra sản phẩm tồn tại trong db
			return ResponseEntity.notFound().build(); // kh tồn tại
		} else {
			return ResponseEntity.ok(pService.findByProductId(id));
		}
	}

	// Tìm kiếm và trả về danh sách các sản phẩm dựa trên từ khóa tìm kiếm
	@GetMapping("/search")
	// tham số "kw" cho phép truy cập và sử dụng giá trị của tham số truy vấn "kw" trong URL.
	public List<Product> searchProduct(@RequestParam("kw") Optional<String> kw){
		String keyword = kw.orElse(null);
		// kiểm tra xem có từ khóa tìm kiếm được cung cấp không. Nếu có, từ khóa được gán vào biến keyword
		if(keyword != null) {
			return pService.findByName("%"+keyword+"%");
		}else { // nếu không keyword = null
			return pService.findAll();
		}
	}
	
	//tạo mới một sản phẩm dựa trên dữ liệu được gửi đến trong phần thân của yêu cầu POST
	@PostMapping("")
	//Tham số data được đánh dấu với @RequestBody, cho phép truy cập và sử dụng dữ liệu được gửi đến trong phần thân của yêu cầu HTTP.
	public Product postProduct(@RequestBody JsonNode data) { 
		return pService.save(data);
	}
	
	@PutMapping("/{id}")
	//Tham số id được truyền vào qua @PathVariable, cho phép truy cập vào giá trị của biến đường dẫn trong URL
	//Tham số data được đánh dấu với @RequestBody, cho phép truy cập và sử dụng dữ liệu được gửi đến trong phần thân của yêu cầu HTTP.
	//Trong phương thức này,dữ liệu được truyền dưới dạng JsonNode và được gọi là data.
	public ResponseEntity<Product> putProduct(@PathVariable("id") Long id, @RequestBody JsonNode data) {
		if (!pService.existsById(id)) { // ktra xem sản phẩm tồn tại trong db kh
			return ResponseEntity.notFound().build(); // kh tồn tại
		} else {
			return ResponseEntity.ok(pService.save(data)); // Product đã được cập nhật trong cơ sở dữ liệu
		}
	}

	@DeleteMapping("/{id}")
	//Tham số id được truyền vào qua @PathVariable, cho phép truy cập vào giá trị của biến đường dẫn trong URL
	public void deleteProduct(@PathVariable("id") Long id) {
			Product product = pService.findById(id); //gọi pService.findById(id) để lấy sản phẩm từ db 
			String images = product.getImages(); //Dữ liệu hình ảnh của sản phẩm được lấy từ thuộc tính images
			//chuyển đổi chuỗi JSON thành một danh sách các tên tệp hình ảnh
			TypeReference<List<String>> typeString = new TypeReference<List<String>>() {
			};
			ObjectMapper mapper = new ObjectMapper();
			try {
				//sử dụng vòng lặp để xóa tệp hình ảnh, trừ tệp hình ảnh có tên là "logo.jpg"
				List<String> list = mapper.readValue(images, typeString);
				System.out.println(list);
				for(String filename : list) {
					if(!filename.equalsIgnoreCase("logo.jpg")) {
						uService.delete("product", filename);
					}
				}
				//danh sách các mục của sản phẩm (ProductCategory) liên quan được lấy từ db
				List<ProductCategory> productCates = pService.findByProductId(id);
				for(ProductCategory productCate : productCates) { // sử dụng vòng lặp để xóa các sản phẩm
					pService.deleteProductCateById(productCate.getId());
				}
				pService.deleteById(id); // sản phẩm đã đc xóa bằng cách gọi pService.deleteById(id)
			} catch (Exception e) {
			}
	}
	
	//xóa mục sản phẩm khỏi db
	@DeleteMapping("/productcategory/{id}")
	// id được truyền vào qua @PathVariable, cho phép truy cập vào giá trị của biến đường dẫn trong URL.
	public void deleteProductCategory(@PathVariable("id") Long id){
		pService.deleteProductCateById(id); // gọi pService.deleteProductCateById(id) để xóa mục sản phẩm với ID tương ứng từ db
	}
	
	
	@PostMapping("/productcategory")
	/*
	 * productCates được truyền vào qua @RequestBody, cho phép truy cập vào dữ liệu gửi đến trong phần thân yêu cầu HTTP. 
	 * Đối tượng productCates được chuyển đổi từ định dạng JSON thành đối tượng ProductCategory.
	 */
	public ProductCategory postProductCategory(@RequestBody ProductCategory productCates) {
		//gọi pService.saveProductCates(productCates) để lưu mục sản phẩm vào db và trả về đối tượng ProductCategory đã được lưu.
		return pService.saveProductCates(productCates);
	}
}











