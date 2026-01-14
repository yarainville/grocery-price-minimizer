# Grocery Price Minimizer

A web app that helps you find the cheapest store for your groceries by comparing prices across Walmart, Metro, Maxi, and IGA.

## What It Does

- Search for grocery products (milk, eggs, etc.)
- Add products to your shopping basket
- Enter prices you see at different stores
- Calculate which store gives you the lowest total

## Tech Stack

- **Backend:** Java, Spring Boot, MySQL
- **Frontend:** HTML, CSS, JavaScript, React
- **APIs:** OpenFoodFacts, Google Custom Search

## Features

**Working:**
- Search products (900+ milk and egg products in database)
- Add products to basket
- Enter prices for each store
- Calculate best store for your basket
- Simple web UI

**Coming Soon:**
- OpenAI integration to filter bad Google search results (current search results aren't great)
- User accounts
- Price history

## Setup

**Requirements:**
- Java 17+
- MySQL
- Google Custom Search API key

**Steps:**

1. Clone the repo
```bash
git clone https://github.com/yarainville/grocery-price-minimizer.git
cd grocery-price-minimizer
```

2. Create MySQL database
```sql
CREATE DATABASE grocery_price_minimizer;
```

3. Set environment variables (or edit `application.properties`)
```
DB_PASSWORD=your_password
GOOGLE_SEARCH_API_KEY=your_key
GOOGLE_SEARCH_CX=your_cx_id
```

4. Run it
```bash
mvn spring-boot:run
```

5. Open browser
```
http://localhost:8080/index.html
```

6. Import sample data
```bash
curl -X POST http://localhost:8080/products/import/milk
curl -X POST http://localhost:8080/products/import/eggs
```

## How It Works

1. Search for products - common items like milk/eggs come from local database (fast), other items use OpenFoodFacts API
2. Click "Find in Stores" to search Google for the product at each store
3. Click the links to verify and see the actual price on the store website
4. Enter the prices you see
5. Add products to your basket
6. Click "Calculate Best Store" to see which store is cheapest

**Note:**
- Google search results aren't very accurate right now - lots of junk results. Planning to add OpenAI to filter them automatically.
- Currently only milk and eggs are in the database (900+ products). Will add more categories eventually.

## Project Structure

- `src/main/java/.../controller/` - REST API endpoints
- `src/main/java/.../service/` - Business logic
- `src/main/java/.../model/` - Data models
- `src/main/resources/static/` - Web UI
- `src/main/resources/csv/` - Product data files

## Database

5 tables:
- `products` - All products (900+)
- `baskets` - Shopping baskets
- `basket_items` - Items in baskets
- `store_products` - Prices at each store
- `selected_products` - Old table (not used much)

## API Endpoints

**Products:**
- `GET /products/search?q=milk` - Search
- `GET /products/{id}/find-in-stores` - Find at stores
- `POST /products/import/milk` - Import data

**Baskets:**
- `POST /baskets` - Create basket
- `GET /baskets/{id}/items` - View basket
- `POST /baskets/{id}/items` - Add item
- `DELETE /baskets/{id}/items/{itemId}` - Remove item
- `GET /baskets/{id}/best-store` - Calculate best

**Prices:**
- `POST /store-products` - Save price

## Known Issues

- Google search results include lots of non-product pages
- No user authentication yet
- UI is basic

## Author

Yuli-Anne Rainville  
Computer Science Student, University of Ottawa  
yrain012@uottawa.ca

## License

Educational project - feel free to use for learning