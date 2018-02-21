package com.company.samazon;


import com.company.samazon.Models.AppUser;
import com.company.samazon.Models.Cart;
import com.company.samazon.Models.Product;
import com.company.samazon.Repositories.CartRepository;
import com.company.samazon.Repositories.ProductRepository;
import com.company.samazon.Repositories.UserRepository;
import com.company.samazon.Security.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Controller
public class HomeController {

    @Autowired
    private UserService userService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartRepository cartRepository;

    ////////(ADMIN) USE TO CREATE OR EDIT PRODUCT

    @GetMapping("/newproduct")
    public String newProduct(Model model){
        model.addAttribute("product", new Product());
        return "ProductForm";
    }

    @PostMapping("/newproduct")
    public String processProduct(@Valid @ModelAttribute("product") Product product, BindingResult result){
        if(result.hasErrors()){
            return "ProductForm";
        }
        userService.saveProduct(product);
        return "redirect:/";
    }

    //Edit product, using product id
    @RequestMapping("/edit/{id2}")
    public String editProduct(@PathVariable("id2") long id2, Model model){
        Product product = productRepository.findOne(id2);
        model.addAttribute("product", product);
        return "ProductForm";
    }


    //////////////////////////(permit all)
    @RequestMapping("/")
    public String homePage(Model model){
        model.addAttribute("products", productRepository.findAll());
        return "Home";
    }

//    @RequestMapping("/product/{id}/{id2}")
//    public String productDetailsUser(@PathVariable("id") long id, @PathVariable("id2") long id2, Model model){
//        AppUser appUser = userRepository.findOne(id);
//        Product product = productRepository.findOne(id2);
//        model.addAttribute("product", product);
//        model.addAttribute("appUser", appUser);
//        return "ProductPageUser";
//    }


    @RequestMapping("/product/{id2}")
    public String productDetails(@PathVariable("id2") long id2, Model model){
        Product product = productRepository.findOne(id2);
        model.addAttribute("product", product);
        return "ProductPage";
    }

    @RequestMapping("/searchresults")
    public String getSearchResults(Model model){
        model.addAttribute("temp", "temp");
        return "SearchResult";
    }

/////////////////////////// New User, Login and User Details (Order History) (CUSTOMER)

    @RequestMapping("/login")
    public String login(Model model){
        return "Login";
    }

    @GetMapping("/newuser")
    public String newUser(Model model){
        model.addAttribute("appuser", new AppUser());
        return "UserForm";
    }

    @PostMapping("/newuser")
    public String processUser(@Valid @ModelAttribute("appuser") AppUser appuser, BindingResult result){
        if(result.hasErrors()){
            return "UserForm";
        }
        userService.saveCustomer(appuser);
        return "redirect:/";
    }


    //Use appUser.id to get appUser
    @RequestMapping("/user/{id}")
    public String userDetails(@PathVariable("id") long id, Model model){
        AppUser appuser = userService.findById(id);
        model.addAttribute("carts", appuser.getCarts());
        model.addAttribute("appuser", appuser);
        return "UserDetails";
    }

    /////////////////////////////////Checkout  (CUSTOMER)


    //appUser.id used to checkout
    @RequestMapping("/checkout/{id}")
    public String checkoutCart(@PathVariable("id") Long id, Model model){
        AppUser appUser = userRepository.findOne(id);
        Cart myCart = userService.getActiveCart(appUser);

        //Below: updating quantities of all products in cart
        userService.CheckoutCart(myCart);

        //Below: Changing cart -> order
        myCart.setStatus("NotActive");

        //Below: new active cart for the next time
        userService.setActiveCart(appUser);

        //Sending information from order to confirmation
        model.addAttribute("cart", myCart);
        model.addAttribute("products", myCart.getProducts());
        model.addAttribute("total", userService.getTotal(myCart));
        return "Confirmation";
    }

    @RequestMapping("/orderdetails/{id}")
    public String viewOrder(@PathVariable("id") long id, Model model){
        Cart cart = cartRepository.findOne(id);
        model.addAttribute("products", cart.getProducts());
        model.addAttribute("total", userService.getTotal(cart));
        return "Confirmation";
    }


 ////////////////////////// Cart (CUSTOMER)


    ///////////******************* MODIFIED METHOD SINCE PUSH
    @RequestMapping("/addtocart/{id}/{id2}")
    public String addToCart(@PathVariable("id") long id, @PathVariable("id2") long id2, Model model){
        AppUser appUser = userRepository.findOne(id);
        Product product = productRepository.findOne(id2);
        Cart cart = userService.getActiveCart(appUser);
        userService.updateCart(product, cart);
        model.addAttribute("cart", cart);
        return "redirect:/cart/{id2}";
    }

    //View Cart based on user id
    @RequestMapping("/cart/{id}")
    public String viewCart(@PathVariable("id") long id, Model model){
        AppUser appUser = userRepository.findOne(id);
        model.addAttribute("appUser", appUser);
        model.addAttribute("total", userService.getTotal(userService.getActiveCart(appUser)));
        model.addAttribute("products", userService.getActiveCart(appUser).getProducts());
        return "Cart";
    }


    ///////////******************* MODIFIED METHOD SINCE PUSH
    @RequestMapping("/remove/{id2}/{id}")
    public String removeItem(@PathVariable("id") long id, @PathVariable("id2") long id2){
        AppUser appUser = userRepository.findOne(id);
        Product product = productRepository.findOne(id2);
        Cart cart = userService.getActiveCart(appUser);
        userService.removeItem(product, cart);
        return "redirect:/cart/{id2}";
    }




}
