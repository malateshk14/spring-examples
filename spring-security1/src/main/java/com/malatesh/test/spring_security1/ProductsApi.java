package com.malatesh.test.spring_security1;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/product")
public class ProductsApi {


  @GetMapping
  public String getProduct(){

    SecurityContext context = SecurityContextHolder.getContext();
    Authentication auth =  context.getAuthentication();

    System.out.println(auth);
    return String.join(",",
        new String[]{ "1","2","3","4" });


  }
}

