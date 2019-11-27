package me.dblab.twitterclone.account;

import org.springframework.http.HttpStatus;
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
    @ResponseStatus(HttpStatus.CREATED)
    public void saveAccount(@RequestBody Account account)  {
        accountService.saveAccount(account);
    }

    // Update User
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void updateAccount(@PathVariable String id, @RequestBody Account account)    {
        accountService.updateAccount(account, id);
    }

    // Delete User
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteAccount(@PathVariable String id)  {
        accountService.deleteAccount(id);
    }

}
