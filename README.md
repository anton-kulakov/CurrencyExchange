# Currency exchange

Currency exchange is a REST API application for managing exchange rates and performing currency conversions. This project is based on the technical specification provided in the Java Backend Roadmap by Sergey Zhukov (link [here](https://zhukovsd.github.io/java-backend-learning-course/projects/currency-exchange/)). 

### 🧱Built with
![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white) ![Java-Servlet](https://img.shields.io/badge/Java%20SERVLET-003545?style=for-the-badge&logo=openjdk&logoColor=white) ![JDBC](https://img.shields.io/badge/JDBC-59666C?style=for-the-badge&logo=Hibernate&logoColor=white) ![SQLite](https://img.shields.io/badge/SQLite-blue?style=for-the-badge&logo=sqlite&logoSize=auto&color=%23003B57) ![APACHE MAVEN](https://img.shields.io/badge/Apache%20Maven-blue?style=for-the-badge&logo=apachemaven&logoSize=auto&color=%23C71A36) ![Apache Tomcat](https://img.shields.io/badge/apache%20tomcat-%23F8DC75.svg?style=for-the-badge&logo=apache-tomcat&logoColor=black) ![Lombok](https://img.shields.io/badge/Lombok-D70A53?style=for-the-badge&logo=Lombok&logoColor=white)
### ⚙️Application features 
- add currencies and exchange rates
- update exchange rates
- convert amounts of money between different currencies based on the latest exchange rates

### 📝REST API description

1. **Database:**

The project uses SQLite as the database, which allows embedding a file with populated tables into the project resources for the application's functionality. 

Database diagram:
![280](https://github.com/user-attachments/assets/731468ad-557a-4de6-b0d5-efd29cbdd81d)

2. **REST API endpoints:**

>`GET /currencies` - returns list of all currencies

Example of response:
```json
[
    {
        "id": 0,
        "name": "United States dollar",
        "code": "USD",
        "sign": "$"
    },   
    {
        "id": 0,
        "name": "Euro",
        "code": "EUR",
        "sign": "€"
    }
]
```
>`GET /currency/EUR` - returns a specific currency.
>
The currency code is specified in the query address 

Example of response:
```json
{
    "id": 0,
    "name": "Euro",
    "code": "EUR",
    "sign": "€"
}
```

> `POST /currencies` - adding a new currency to the database. 
> 
> The data is sent in the request body as form fields (x-www-form-urlencoded). The form fields are name, code, and sign. 

Example of response: a JSON representation of the inserted record in the database, including its ID:
```json
{
    "id": 0,
    "name": "Euro",
    "code": "EUR",
    "sign": "€"
}
```
>`GET /exchangeRates`  - returns list of all exchange rates

Example of response:
```json
[
    {
        "id": 0,
        "baseCurrency": {
            "id": 0,
            "name": "United States dollar",
            "code": "USD",
            "sign": "$"
        },
        "targetCurrency": {
            "id": 1,
            "name": "Euro",
            "code": "EUR",
            "sign": "€"
        },
        "rate": 0.99
    }
]
```
> `GET /exchangeRate/USDRUB` - returns a specific exchange rate.
> 
> The currency pair is specified by consecutive currency codes in the request URL
Example of response:
```json
{
    "id": 0,
    "baseCurrency": {
        "id": 0,
        "name": "United States dollar",
        "code": "USD",
        "sign": "$"
    },
    "targetCurrency": {
        "id": 1,
        "name": "Euro",
        "code": "EUR",
        "sign": "€"
    },
    "rate": 0.99
}
```
> `POST /exchangeRates` - adding a new exchange rate to the database. 
> 
> The data is sent in the request body as form fields (x-www-form-urlencoded). The form fields are baseCurrencyCode, targetCurrencyCode, and rate. 
> 
> Example form fields:
> - baseCurrencyCode - USD
> - targetCurrencyCode - EUR
> - rate - 0.99

Example of response: a JSON representation of the inserted record in the database, including its ID:
```json
{
    "id": 0,
    "baseCurrency": {
        "id": 0,
        "name": "United States dollar",
        "code": "USD",
        "sign": "$"
    },
    "targetCurrency": {
        "id": 1,
        "name": "Euro",
        "code": "EUR",
        "sign": "€"
    },
    "rate": 0.99
}
```

>`PATCH /exchangeRate/USDRUB` - updating an existing exchange rate in the database. 
>
>The currency pair is specified by consecutive currency codes in the request URL. The data is sent in the request body as form fields (x-www-form-urlencoded). 
>
>The only form field is rate.

Example of response: a JSON representation of the inserted record in the database, including its ID:
```json
{
    "id": 0,
    "baseCurrency": {
        "id": 0,
        "name": "United States dollar",
        "code": "USD",
        "sign": "$"
    },
    "targetCurrency": {
        "id": 1,
        "name": "Euro",
        "code": "EUR",
        "sign": "€"
    },
    "rate": 0.99
}
```

>`GET /exchange?from=BASE_CURRENCY_CODE&to=TARGET_CURRENCY_CODE&amount=$AMOUNT` - calculating the conversion of a specific amount of funds from one currency to another. 

Example of request: 
```
GET /exchange?from=USD&to=AUD&amount=10
```
Example of response: 
```json
{
    "baseCurrency": {
        "id": 0,
        "name": "United States dollar",
        "code": "USD",
        "sign": "$"
    },
    "targetCurrency": {
        "id": 1,
        "name": "Australian dollar",
        "code": "AUD",
        "sign": "A€"
    },
    "rate": 1.45,
    "amount": 10.00,
    "convertedAmount": 14.50
}
```
Obtaining the exchange rate for conversion can occur through one of three scenarios. Let's assume we're making a transfer from currency A to currency B:

- There is a currency pair AB in the ExchangeRates table – we take its rate
- There is a currency pair BA in the ExchangeRates table – we take its rate and calculate the inverse to obtain AB
- There are currency pairs USD-A and USD-B in the ExchangeRates table – we calculate the rate for AB from these rates

### 👩‍💻Launching the application in IntelliJ IDEA

To run the application, make sure that you have Java (JDK), Apache Maven and Apache Tomcat installed on your computer.
