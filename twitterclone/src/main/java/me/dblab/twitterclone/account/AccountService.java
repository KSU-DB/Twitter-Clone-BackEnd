package me.dblab.twitterclone.account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class AccountService {

    private Logger logger = LoggerFactory.getLogger(AccountService.class);

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Mono<Account> getAccount(String id) {
        return accountRepository.findById(id);
    }

    public void saveAccount(Account account) {
        Mono.just(account)
                .flatMap(accountRepository::save)
                .subscribe(x -> logger.info("x : " + x));
    }

    public void updateAccount(Account account, String id) {
        accountRepository.findById(id)
                .flatMap(modifiedAccount -> {
                    modifiedAccount.update(account);
                    return accountRepository.save(modifiedAccount);
                })
                .subscribe();
    }

    public void deleteAccount(String id) {
        accountRepository.deleteById(id);
    }


}
