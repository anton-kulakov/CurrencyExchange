# Currency exchange

Currency exchange is a REST API application for managing exchange rates and performing currency conversions. This project is based on the technical specification provided in the Java Backend Roadmap by Sergey Zhukov (link [here](https://zhukovsd.github.io/java-backend-learning-course/projects/currency-exchange/)). 

## 🧱Built with
![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white) ![Java-Servlet](https://img.shields.io/badge/Java%20SERVLET-003545?style=for-the-badge&logo=openjdk&logoColor=white) ![JDBC](https://img.shields.io/badge/JDBC-59666C?style=for-the-badge&logo=Hibernate&logoColor=white) ![SQLite](https://img.shields.io/badge/SQLite-blue?style=for-the-badge&logo=sqlite&logoSize=auto&color=%23003B57) ![APACHE MAVEN](https://img.shields.io/badge/Apache%20Maven-blue?style=for-the-badge&logo=apachemaven&logoSize=auto&color=%23C71A36) ![Apache Tomcat](https://img.shields.io/badge/apache%20tomcat-%23F8DC75.svg?style=for-the-badge&logo=apache-tomcat&logoColor=black) ![Lombok](https://img.shields.io/badge/Lombok-D70A53?style=for-the-badge&logo=Lombok&logoColor=white)
## ⚙️Application features 
- add currencies and exchange rates
- update exchange rates
- convert amounts of money between different currencies based on the latest exchange rates

## 📝REST API description

1. **Database:**

The project uses SQLite as the database, which allows embedding a file with populated tables into the project resources for the application's functionality. 

Database diagram:

<img src="https://github.com/user-attachments/assets/731468ad-557a-4de6-b0d5-efd29cbdd81d" alt="Описание" width="300" />

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

## 👩‍💻Launching the application in IntelliJ IDEA

To run the application, make sure that you have Java (JDK), Apache Maven and Apache Tomcat installed on your computer.

1. Go to the configurations menu
   
![1  Открываем конфигуратор](https://github.com/user-attachments/assets/1d45b748-35fa-4588-84fc-8f306894dc60)

2. Select "Edit configurations"

![2  Раскрываем конфигуратор](https://github.com/user-attachments/assets/f2050cf9-95f5-4d7b-80a3-2d71b7979f8f)

3. Click the plus sign

![3  Нажимаем плюс](https://github.com/user-attachments/assets/dd20b649-6046-47d0-961d-6593112945b8)

4. Select Tomcat -> Local

![4  Выбираем томкат](https://github.com/user-attachments/assets/c9ec29f3-ebab-4ba9-a4a0-14689793d7db)

5. Select Tomcat as a Application server

![5  Выбираем томкат2](https://github.com/user-attachments/assets/496d69d4-c6d2-4e4e-9b0e-9962e1730cc7)

6. Click "Fix"

![6  Нажимаем фикс](https://github.com/user-attachments/assets/d5b22061-b465-4e71-ba95-0d17e70914fb)

7. Select CurrencyExchange:war exploded

![7  Выбираем вар эксплодед](https://github.com/user-attachments/assets/079904de-ea5b-4ad5-a047-13486ad8cd62)

8. Remove "CurrencyExchange_war_exploded" from the application context

![8  Убираем лишнее](https://github.com/user-attachments/assets/f110b8d9-0d64-417b-b4a9-6065c13d5049)

9. Click "Run"

![9  Run](https://github.com/user-attachments/assets/77e54bbb-e25e-4f69-bd58-7aac4e6e133b)

Apache Maven will build the project and run it using Tomcat.

