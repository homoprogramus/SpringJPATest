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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import springJPATest.Application;
import springJPATest.entities.Account;
import springJPATest.repositories.AccountRepository;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

/**
 * @author Joel Voiselle
 * @version 1.0
 */
@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@RunWith(SpringJUnit4ClassRunner.class)//for unit testing
@SpringApplicationConfiguration(classes = Application.class)//give class configuration for auto-configuration
@WebAppConfiguration//marking for web app auto-configuration
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class AccountControllerTest
{
    private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            Charset.forName("utf8"));//contentType for checking http response content-type

    private MockMvc mockMvc;//mock object

    private HttpMessageConverter mappingJackson2HttpMessageConverter;//map JSON to HTTP message

    private Account setupAccount;

    @Autowired
    private AccountRepository accountRepository;//create an Account repository for testing

    @Autowired
    private WebApplicationContext webApplicationContext;//create a web application context

    @Autowired
    private void setConverters(HttpMessageConverter<?>[] converters)
    {
        mappingJackson2HttpMessageConverter = Arrays.asList(converters).stream().filter(
                hmc -> hmc instanceof MappingJackson2HttpMessageConverter).findAny().get();
    }

    @Before//launched before tests
    public void setUp() throws Exception
    {
        setupAccount = new Account("setup username", "setup password");

        mockMvc = webAppContextSetup(webApplicationContext).build();//instantiate the mock object

        accountRepository.deleteAllInBatch();//empty the Account repository
        accountRepository.save(setupAccount);//add a new Account in the repository
    }

    @Test
    public void testAdd() throws Exception
    {
        String username = "added user";
        String password = "added password";

        String accountJSON = json(new Account(username, password));//create a JSON from a new Account object
        mockMvc.perform(put("/account")//use mock object for HTTP PUT
        .contentType(contentType)//set the content type
        .content(accountJSON))//set the content
        .andExpect(status().isOk())//assert the object is created
        .andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$.username", is(username)))
        .andExpect(jsonPath("$.password", is(password)))
        .andExpect(jsonPath("$.id", is(2)))//assert the object has id = 2 (we already have one in the database)
        ;
    }

    @Test
    public void testGetAll() throws Exception
    {
        mockMvc.perform(get("/account"))//use mock object to interrogate the REST service
                .andExpect(status().isOk())//check the return status is OK
                .andExpect(content().contentType(contentType))//check the return content is JSON
                .andExpect(jsonPath("$", hasSize(1)))//check there is one child in the returned JSON (we have one in the repository)
                .andExpect(jsonPath("$[0].username", is(setupAccount.getUsername())))//check username is the same as set in the repository
                .andExpect(jsonPath("$[0].password", is(setupAccount.getPassword())))//check password is the same as set in the repository
                .andExpect(jsonPath("$[0].id", is(1)));//check id is 1 (we have only one account in the repository)
    }

    /*
     * Convert an object to JSON String
     */
    private String json(Object o) throws IOException
    {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        mappingJackson2HttpMessageConverter.write(o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }
}