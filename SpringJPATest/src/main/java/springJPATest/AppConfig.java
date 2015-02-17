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

package springJPATest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * @author Joel Voiselle
 * @version 1.0
 */
@SuppressWarnings({"SpringFacetCodeInspection", "UnusedDeclaration"})//IDE marking, useless
@Component//mark the class as an application component
@PropertySource("classpath:springJPATest/application.properties")//set the source of the properties
@Profile("!test")//if the active profile is not 'test' configure the app to connect to a postgresql database
public class AppConfig
{
    private String username;
    private String password;
    private String url;

    @Autowired//launched at application boot
    public AppConfig(@Value("${springJPATest.dbname}") String dbName, @Value("${springJPATest.username}") String username,
                     @Value("${springJPATest.password}") String password, @Value("${springJPATest.dbhost}") String dbhost,
                     @Value("${springJPATest.dbport}") String dbport)//@Value maps the variable value from the file to the
                                                                     //Java variable
    {
        this.username = username;
        this.password = password;

        /*
         * This part is used for getting the database port from the environment variable POSTGRESQL_PORT_5432_TCP_PORT.
         * It was created because PostgreSQL and Wildfly are on two separate Docker container
         */
        if("default".equals(dbport))
        {
            dbport = System.getenv("POSTGRESQL_PORT_5432_TCP_PORT");
        }

        url = "jdbc:postgresql://" + dbhost + ":" + dbport + "/" + dbName;
    }

    @Bean//give the database connection
    public DataSource dataSource()
    {
        return DataSourceBuilder.create()
                .url(url)
                .username(username)
                .password(password)
                .build();
    }
}