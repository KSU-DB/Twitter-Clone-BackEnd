package me.dblab.twitterclone.account;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/users")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<Account> getAccount(@PathVariable String id) {
        return accountService.getAccount(id);
    }

    // Registration
    @PostMapping
    public Mono<ResponseEntity> saveAccount(@RequestBody Mono<Account> account) {
        return accountService.saveAccount(account);
    }

    @PostMapping("/login")
    public Mono<ResponseEntity> login(@RequestBody Mono<Account> account) {
        return accountService.login(account);
    }

    // Update User
    @PutMapping("/{id}")
    public Mono<ResponseEntity> updateAccount(@PathVariable String id, @RequestBody Mono<Account> account) {
        return accountService.updateAccount(id, account);
    }

    // Delete User
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteAccount(@PathVariable String id)  {
        accountService.deleteAccount(id);
    }

}
