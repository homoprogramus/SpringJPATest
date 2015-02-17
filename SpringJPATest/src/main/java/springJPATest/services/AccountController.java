/*
 * Copyright (c) 2015 Joel Voiselle
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package springJPATest.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import springJPATest.entities.Account;
import springJPATest.repositories.AccountRepository;

import java.util.Collection;

/**
 * @author Joel Voiselle
 * @version 1.0
 */
@RestController//REST service annotation
@RequestMapping("/account")//path mapping
public class AccountController
{
    private final AccountRepository accountRepository;

    @Autowired//launched at application boot
    public AccountController(AccountRepository accountRepository)
    {
        this.accountRepository = accountRepository;
    }

    @RequestMapping(method = RequestMethod.PUT)//expose this method as a service to HTTP PUT
    public Account add(@RequestBody Account input)//@RequestBody map JSON from HTTP request body to Account input object
    {
        return accountRepository.saveAndFlush(input);
    }

    @RequestMapping(method = RequestMethod.GET)//expose this method as a service to HTTP GET
    public Collection<Account> getAll()
    {
        return accountRepository.findAll();
    }
}
