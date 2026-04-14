# Auction System

A comprehensive auction system built with Spring Boot and MySQL, featuring three types of users: Admin, Seller, and Buyer.

## Features

### Admin Features
- Approve/reject products for auction
- Manage users (activate/deactivate)
- Monitor system statistics
- View all auctions and bids

### Seller Features
- Add products for auction
- Set starting prices and reserve prices
- Schedule auction start and end times
- Track product status and bids

### Buyer Features
- Browse available auctions
- Place bids on products
- Track bidding history
- Search for specific products
- Live highest-bid updates on auction detail page (API polling)

### Concurrency and Consistency
- Strong consistency for bid placement using database row-level locking (`PESSIMISTIC_WRITE` on product row)
- Atomic bid transaction: previous winning bid status update + new bid insert + product highest update in one transaction
- Rollback-safe behavior to prevent partial state updates
- Optimistic versioning on products using `@Version`
- Fast highest-bid and timeline queries via database indexes

## Technology Stack

- **Backend**: Spring Boot 1.1.8
- **Database**: MySQL 8.0
- **ORM**: Spring Data JPA
- **Frontend**: Thymeleaf + Bootstrap 5
- **Build Tool**: Maven

## Prerequisites

1. **Java 8** or later
2. **MySQL 8.0** or later
3. **Maven 3.6** or later

## Setup Instructions

### 1. Database Setup

1. Install MySQL and start the service
2. Create a database (the application will create it automatically):
   ```sql
   CREATE DATABASE auction_system;
   ```
3. Update database credentials in `src/main/resources/application.yml`:
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/auction_system?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
       username: root
       password: your_password
   ```

### 2. Application Setup

1. Clone or download the project
2. Navigate to the project directory:
   ```bash
   cd auction-system
   ```
3. Build and run the application:
   ```bash
   mvn clean spring-boot:run
   ```

### 3. Access the Application

Once the application is running, open your browser and go to:
```
http://localhost:8080
```

## Security Features

### Password Security
- **BCrypt Hashing**: All passwords are encrypted using BCrypt with salt
- **Automatic Migration**: Existing plain text passwords are automatically migrated to hashed format on startup
- **Password Change**: Users can securely change their passwords through the profile page
- **Validation**: Password strength requirements (minimum 6 characters)

### Authentication
- Session-based authentication
- Role-based access control (Admin, Seller, Buyer)
- Secure password validation

## Default User Accounts

The application creates the following default accounts:

### Admin Account
- **Username**: `admin`
- **Password**: `admin123`
- **Email**: `admin@auction.com`

### Sample Seller Account
- **Username**: `seller1`
- **Password**: `seller123`
- **Email**: `seller1@auction.com`

### Sample Buyer Account
- **Username**: `buyer1`
- **Password**: `buyer123`
- **Email**: `buyer1@auction.com`

**Note**: All passwords are securely hashed using BCrypt encryption.

## User Workflows

### For Sellers
1. Login with seller credentials
2. Go to "Add Product" to list items for auction
3. Set product details, starting price, and auction schedule
4. Wait for admin approval
5. Monitor bids once approved

### For Buyers
1. Login with buyer credentials
2. Browse "Active Auctions"
3. View auction details and place bids
4. Track your bids in "My Bids"

### For Admins
1. Login with admin credentials
2. Review pending products for approval
3. Manage users and system settings
4. Monitor auction activities

## Database Schema

The application creates the following main tables:

- `users` - Store user information
- `products` - Store auction products
- `bids` - Store bid information

## API Endpoints

### Authentication
- `GET /login` - Login page
- `POST /login` - Process login
- `GET /register` - Registration page
- `POST /register` - Process registration
- `GET /logout` - Logout

### Admin Routes
- `GET /admin/dashboard` - Admin dashboard
- `GET /admin/products` - Manage products
- `POST /admin/product/approve/{id}` - Approve product
- `POST /admin/product/reject/{id}` - Reject product

### Seller Routes
- `GET /seller/dashboard` - Seller dashboard
- `GET /seller/products` - View seller's products
- `GET /seller/product/add` - Add product form
- `POST /seller/product/add` - Process add product

### Buyer Routes
- `GET /buyer/dashboard` - Buyer dashboard
- `GET /buyer/auctions` - Browse auctions
- `GET /buyer/auction/{id}` - View auction details
- `POST /buyer/auction/{id}/bid` - Place bid

### Concurrent Bidding APIs
- `POST /api/auctions/{id}/bids` - Place bid (JSON body: `bidAmount`, optional `bidderId` for load tests)
- `GET /api/auctions/{id}/highest-bid` - Current highest bid snapshot

## Consistency Model

This project now uses strong consistency for each auction's bid path.

- Every bid transaction locks the target product row before checking and updating the highest bid.
- Concurrent stale bids are rejected with `409` rather than overwriting newer winning bids.
- `currentHighestBid`, winning bid status, and bid history are updated atomically.

Eventual consistency can still be used for non-critical, read-heavy projections (analytics dashboards, recommendation feeds), but winner selection and highest-bid updates must remain strongly consistent.

## Scalability Notes

- Partition workload by auction ID (natural sharding key).
- Keep bid writes directed to a primary DB node per shard for ordering guarantees.
- Use read replicas for browse/search pages while bid write path remains strongly consistent.
- Apply backpressure/rate limits when bid spikes exceed DB lock throughput.

## Load Testing (50-100 Concurrent Bidders)

Use the k6 script in [load-tests/concurrent-bidders.js](load-tests/concurrent-bidders.js).

```bash
k6 run \
   -e BASE_URL=http://localhost:9090 \
   -e AUCTION_ID=1 \
   -e START_BIDDER_ID=3 \
   -e BIDDER_COUNT=100 \
   -e VUS=75 \
   -e DURATION=45s \
   load-tests/concurrent-bidders.js
```

Expected: responses are primarily `201` and `409`, with no `5xx` errors.

## Configuration

### Application Properties
The main configuration is in `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/auction_system?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
    username: root
    password: password
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

## Troubleshooting

### Common Issues

1. **Database Connection Error**
   - Ensure MySQL is running
   - Verify database credentials in `application.yml`
   - Check if the database exists

2. **Port Already in Use**
   - The application runs on port 8080 by default
   - Change the port in `application.yml`:
     ```yaml
     server:
       port: 8081
     ```

3. **Build Errors**
   - Ensure Java 8+ is installed
   - Check Maven configuration
   - Run `mvn clean install` first

## Future Enhancements

- Email notifications for auction events
- Real-time bidding updates
- Payment integration
- Advanced search and filtering
- Mobile responsive design improvements
- REST API for mobile applications

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## License

This project is created for educational purposes.
