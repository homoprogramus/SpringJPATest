=========================================
Spring with PostgreSQL, Hibernate and JPA
=========================================

There are many resources about **Spring** with memory database but only a few lines in the official documentation about production database. Moreover a lot of resources are present on the internet explaining how to configure a database using some XML files which I wanted to get rid off. This document gives informations about how to use **Spring** with **PostgreSQL** and JPA annotations.

Chapter 1: Projet Structure
===========================

The project is a dummy project reading some user informations in a **PostgreSQL** database (id, username and password) and returning them as JSON to the client by a GET request. It also support a PUT request for adding new data in the database.

The IDE used is **Intellij IDEA** and the projet managment tool is **Gradle**.

The structure of the project is::
    
    SpringJPATest/
    |--src/
    |  |--main/
    |  |  |--java/
    |  |  |  |--springJPATest/
    |  |  |     |--AppConfig.java
    |  |  |     |--Application.java
    |  |  |     |--entities/
    |  |  |        |--Account.java
    |  |  |     |--repositories/
    |  |  |        |--AccountRepository.java
    |  |  |     |--services/
    |  |  |        |--AccountController.java
    |  |  |--resources/
    |  |     |--springJPATest/
    |  |        |--application.properties
    |  |--test/
    |     |--java/
    |     |--resources/
    |--build.gradle

Some folders and files are not shown because created by the IDE or by gradle and are not usefull for us in this scenario.

The main package springJPATest/ contains 3 sub-packages: entities/, repositories/ and services/ which respectively contains the entities as java class (used by Hibernate for persistance), the entity repositories as Interface (used also by Hibernate) and the REST services.

The application.properties in resources contains some variable for configuring the database and Hibernate.

Chapter 2: Gradle
=================

The gradle file base is the same as suggested on the Spring website::

    buildscript {
        repositories {
            maven { url "https://repo.spring.io/libs-release" }
            mavenLocal()
            mavenCentral()
        }
        dependencies {
            classpath("org.springframework.boot:spring-boot-gradle-plugin:1.2.1.RELEASE")
        }
    }
    
    apply plugin: 'java'
    apply plugin: 'eclipse'
    apply plugin: 'idea'
    apply plugin: 'spring-boot'

    jar {
        baseName = 'test'
        version = '0.0.1'
    }

    repositories {
        mavenLocal()
        mavenCentral()
        maven { url "https://repo.spring.io/libs-release" }
    }

    dependencies {
        compile("org.springframework.boot:spring-boot-starter-web")
        testCompile("junit:junit")
    }

    task wrapper(type: Wrapper) {
        gradleVersion = '1.11'
    }

We are going to modify some stuff.

First we want a *.war* so we modify the jar directive to::

    war {
        baseName = 'test'
        version = '0.0.1'
    }

and apply the *war* plugin :code:`apply plugin: 'war'`.

The *eclipse* and *idea* plugin are gradle plugin allowin the automated creation of IDE project configuration files. They can be safely removed.

The builded *.war* will be deployed on **Wildfly** already containing a component similar to *spring-boot-starter-tomcat* module, thus we have to exclude it from *spring-boot-starter-web* :code:`exclude module: "spring-boot-starter-tomcat"`.

We need to include the *javax.servlet-api* :code:`compile("javax.servlet:javax.servlet-api")`.

To use JPA the package *spring-boot-starter-data-jpa* is required :code:`compile("org.springframework.boot:spring-boot-starter-data-jpa")`.

We are going to connect to a **PostgreSQL** so we need to add a dependency :code:`compile("org.postgresql:postgresql:9.4-1200-jdbc41")`. Because *slf4j* is already provided by *spring-boot-starter-web* we need to exclude it from *postgresql* :code:`exclude group 'org.slf4j'`.

The line :code:`providedRuntime("org.springframework.boot:spring-boot-starter-tomcat")` is used by the IDE for launching an integrated **Tomcat** server for testing and not necessary for building a running *.war*.

After these modification the file content should looks like::

    buildscript {
        repositories {
            maven { url "https://repo.spring.io/libs-release" }
            mavenLocal()
            mavenCentral()
        }
        dependencies {
            classpath("org.springframework.boot:spring-boot-gradle-plugin:1.2.1.RELEASE")
        }
    }
    
    apply plugin: 'java'
    apply plugin: 'idea'
    apply plugin: 'spring-boot'
    apply plugin: 'war'

    war {
        baseName = 'test'
        version = '0.0.1'
    }

    repositories {
        mavenLocal()
        mavenCentral()
        maven { url "https://repo.spring.io/libs-release" }
    }

    dependencies {
        compile("org.springframework.boot:spring-boot-starter-web") {
            exclude module: "spring-boot-starter-tomcat"
        }
        compile("javax.servlet:javax.servlet-api")
        compile("org.springframework.boot:spring-boot-starter-data-jpa")
        compile("org.postgresql:postgresql:9.4-1200-jdbc41") {
            exclude group 'org.slf4j'
        }
        testCompile("junit:junit")
    }

    task wrapper(type: Wrapper) {
        gradleVersion = '1.11'
    }

Chapter 3: Entities
===================

We start by creating entities, in our case there is only one, Account in the *Account.java* file in the *entities/* repository. Each entity is a class marked down with :code:`@Entity`. Entities are simple POJO with a compulsory void constructor for **Hibernate**. We add an ID (Long) and mark it with :code:`@Id`, we want to automatically generate the ID so we mark it with :code:`@GeneratedValue(strategy = GenerationType.IDENTITY)`.

Our Account entity looks like::

    @Entity
    public class Account
    {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        public String username;
        public String password;
        
        public Account(){}
        
        public Account(String username, String password)
        {
            this.username = username;
            this.password = password;
        }
        
        //... Getter and Setter ...
   }

If we want to exclude a field from the returned JSON we can simply add :code:`@JsonIgnore`. The corresponing field will not be present in the created JSON (from POJO) or set to null in the created POJO (from JSON).

Chapter 4: Repositories
=======================

To manage entities persistance we need repositories. Repositories are simple Java interfaces extending *JpaRepository<T, T>*. Each method of the interface is a way to fetch data. There is no need to implement the methods, only define them.
Our repository for *Account* looks like::

    public interface AccountRepository extends JpaRepository<Account, Long>
    {
        Optional<Account> findByUsername(String username);
    }
    
when extending JpaRepository we need to tell the entity class as well as the id class :code:`JpaRepository<Account, Long>`. The methods  return an *Optional<T>* object.

Chapter 5: Services
===================

The REST services are classes annotated with :code:`@Restcontroller`. The url mapping is made with the :code:`@RequestMapping("PATH")`. It can be applied to the class, setting the service root path and to the methods to refine the path.

To use the repositories it is necessary to provide it to the service controller throught the constructor. This constructor is called automatically by *Spring* through the :code:`@Autowired` annotation.

The REST services are simple java methods marked with :code:`@RequestMapping` and defining the HTTP request the method responds to with the annotation :code:`method = RequestMethod.GET|PUT|POST|DELETE`. The request parameter can be mapped to a POJO with :code:`@RequestBody`.

In our example we can add a new account by using the HTTP PUT and a JSON as request content formed like::

    {'username':'user','password':'pass'}

With the corresponding method, the REST controller is::
    
    @RestController
    @RequestMapping("/account")
    public class AccountController
    {
        private final AccountRepository accountRepository;

        @Autowired
        public AccountController(AccountRepository accountrepository)
        {
            this.accountRepository = accountRepository;
        }

        @RequestMapping(method = RequestMethod.PUT)
        public Account add(@RequestBody Account input)
        {
            return accountRepository.saveAndFlush(input);
        }
    }

Chapter 6: Application entry point
==================================

The application entry point is a simple main method. The class containing the main method must be marqued with :code:`@SpringBootApplication` which allow **Spring** autoconfiguration, an automatic scan of the current package as well as all the children packages for entities, controllers, beans and repositories. That is the reason why the class with the main method is at the root of the application.

Because we use a production database we need to configure it. This configuration, as weel as configuring **Hibernate**, is done through the file *application.properties* which is read by another class named *AppConfig* in our example. Because of this externalized configuration the entry point class needs to extends *SpringBootServletInitializer* and override the *configure(SpringApplicationBuilder sab)* method::
    
    @SpringBootApplication
    public class Application extends SpringBootServletInitializer
    {
        public static void main(String[] args)
        {
            SpringApplication.run(Application.class, args);
        }

        @Override
        protected StringApplicationBuilder configure(SpringApplicationBuilder application)
        {
            return application.sources(Application.class)
        }
    }
    
Chapter 7: Application configuration
====================================

The *AppConfig* as previously described, configure the application from an external file. This class is marked :code:`@Component`. To set which file is used for configuration we use the annotation :code:`@PropertySource("classpath:PATH")` giving the path the file resides.

Values from the file can be mapped to Java object properties using :code:`@Value("${value.name}")` in the constructor for example. This constructor is :code:`@Autowired`. By mapping the file values with :code:`@Value()` the become available in the Java object.

Finally to configure the database in the code we need to create a Java Bean returning a *DataSource*. This is done by using a *DataSourceBuilder* giving it the username, password and url of the database. Our AppConfig class is now::

    @Component
    @PropertySource("classpath:springJPATest/Application.properties")
    public class AppConfig
    {
        private String username;
        private String password;
        private String url;

        @Autowired
        public AppConfig(@Value("${springJPATest.dbname}") String dbname, @Value("${springJPATest.username}") Spring username,
                         @Value("${springJPATest.password}") String password, @Value("${springJPATest.dbhost}") Spring dbhost,
                         @value("${springJPATest.dbport}") String dbport)
        {
            this.username = username;
            this.password = password;
            this.url = "jdbc:postgresql://" + dbhost + ":" + dbport + "/" + dbname;
        }
    
        @Bean
        public DataSource dataSource()
        {
            return DataSourceBuilder.create()
                    .url(url)
                    .username(username)
                    .password(password)
                    .build();
        }
    }

Chapter 8: application.properties
=================================

Now we are going to give the application's component their configuration. The *application.properties* file contains the database configuration (username, password, host, port and database name) as well as information for **Hibernate** for table creation and update. The file is self sufisant and looks like::
	
	springJPATest.username=user
	springJPATest.password=password
	springJPATest.dbname=test
	springJPATest.dbhost=postgresql
	springJPATest.dbport=default
	spring.jpa.hibernate.generate-ddl=true
	spring.jpa.hibernate.ddl-auto=update

Be sure the database exist or you can face an *HibernateException* :code:`Caused by: org.hibernate.HibernateException: Access to DialectResolutionInfo cannot be null when 'hibernate.dialect' not set`.

Chapter 9: Unit test
====================

Now we want to add some unit test to our project. The tricky part is to avoid unit testing to change the database (ID auto-increment, add/delete data etc). **Spring** has a nice feature for getting rid of database modifications by running each test method in a transactional way with the :code:`@Transactional` but the ID will still auto-increment resulting in some gap in the database. To avoid issues the idea is to run unit test in a memory based database.

A memory database is automatically launched by **Spring** as soon as there is no *Datasource* Bean and a **h2** or **hsqldb** reference in the *build.gradle* file. We are going to use **hsqldb** and only for unit testing so we had the line :code:`testCompile("org.hsqldb:hsqldb:2.3.2")` in *build.gradle*. To avoid to connect to the production database we mark the class *AppConfig* with the :code:`@Profile("!test")` annotation. This tells **Spring** to use this class only when the launch profile is NOT test.

Now we can make the unit tests. We create a test class for our services as documented in the **Spring** tutorial_.

.. _tutorial: http://spring.io/guides/tutorials/bookmarks/

First of all we have to mark down the class for unit testing, give the class configuration for auto-configuration and marking the class for a web app auto-configuration::

	@RunWith(SpringJUnit4ClassRunner.class)
	@SpringApplicationConfiguration(classes = Application.class)
	@WebAppConfiguration

Then we want to tell the application this test runs under the *test* profile to use the memory based database. To do that we use the annotation :code:`@ActiveProfiles("test")`. We also use :code:`DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)`. This last annotation tells **Spring** the context is dirty after each method launch, this results in a context builded between each method, performances are worse but it ensure each test run in the same starting environment. By using :code:`@Before` we can populate our database with some data before running any test::

	@RunWith(SpringJUnit4ClassRunner.class)
	@SpringApplicationConfiguration(classes = Application.class)
	@WebAppConfiguration//marking for web app auto-configuration
	@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
	@ActiveProfiles("test")
	public class AccountControllerTest
	{
		private MediaType contetnType = new MediaType(MediaType.APPLICATION_JSON.getType(),
				MediaType.APPLICATION_JSON.getSubType(),
				Charset.forName("utf8"));
		private MockMvc mockMvc;
		private Account setupAccount;
		private HttpMessageConverter mappingJackson2HttpMessageConverter;

		@Autowired
		private AccountRepository accountRepository;

		@Autowired
		private WebApplicationContext webApplicationContext;

		@Autowired
		private void setConverters(HttpMessageConverter<?>[] converters)
		{
				mappingJackson2HttpMessageConverter = Arrays.asList(converters).stream().filter(
					hmc -> hmc instanceof MappingJackson2HttpMessageConverter).findAny().get();

		@Before
		public void setUp() throws Exception
		{
			setupAccount = new Account("setup username", "setup password");

			mockMvc = webAppContextSetup(webApplicationContext).build();

			accountRepository.deleteAllInBatch();
			accountRepository.save(setupAccount);
		}

		@Test
		public void testAdd() throws Exception
		{
			String username = "added user";
			String password = "added password";
			
			String accountJSON = json(new Account(username, password));
			mockMvc.perform(put("/account")
			.contentType(contentType)//set the content type
			.content(accountJSON))//set the content
			.andExpect(status().isOk())//assert the object is created
			.andExpect(content().contentType(contentType))
			.andExpect(jsonPath("$.username", is(username)))
			.andExpect(jsonPath("$.password", is(password)))
			.andExpect(jsonPath("$.id", is(2)));
		}

		private String json(Object o) throws IOException
		{
			MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
			mappingJackson2HttpMessageConverter.write(o, MediaType.Application_JSON, mockHttpOutputMessage);
			return mockHttpOutputMessage.getbodyAsString();
		}
	}


