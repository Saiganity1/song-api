# Song API - Deploy to Render (PostgreSQL) + Test in Postman

## 1) Prerequisites
- GitHub repository with this project pushed
- Render account
- Postman installed

## 2) Deploy PostgreSQL on Render
1. In Render dashboard, click **New +** -> **PostgreSQL**.
2. Choose your plan and create the database.
3. After creation, open the DB page and copy these values:
   - **Host**
   - **Port**
   - **Database**
   - **User**
   - **Password**

## 3) Deploy the Spring Boot API on Render
1. Click **New +** -> **Web Service**.
2. Connect your GitHub repo and select this project.
3. Configure:
   - **Runtime**: Docker
   - **Dockerfile Path**: `./Dockerfile`
   - **Branch**: your deploy branch (usually `main`)
4. In **Environment Variables**, add:
   - `DB_USERNAME` = (Postgres User)
   - `DB_PASSWORD` = (Postgres Password)
   - `DATABASE_URL` = `jdbc:postgresql://<HOST>:<PORT>/<DATABASE>`
5. Deploy the service.

Your app should be reachable at:
- `https://<your-render-service>.onrender.com`

## 4) Confirm app is running
Open this in browser (or Postman):
- `GET https://<your-render-service>.onrender.com/velasco/songs`

If it returns `[]` (or a JSON list), deployment is good.

## 5) Postman test collection (manual)
Set a Postman variable:
- `baseUrl = https://<your-render-service>.onrender.com`

### A. Create song
- **Method**: `POST`
- **URL**: `{{baseUrl}}/velasco/songs`
- **Body (raw JSON)**:
```json
{
  "title": "Shape of You",
  "artist": "Ed Sheeran",
  "album": "Divide",
  "genre": "Pop",
  "url": "https://example.com/shape-of-you"
}
```
- Expected: `200 OK` with created song JSON (including `id`)

### B. Get all songs
- **Method**: `GET`
- **URL**: `{{baseUrl}}/velasco/songs`
- Expected: list of songs

### C. Get by ID
- **Method**: `GET`
- **URL**: `{{baseUrl}}/velasco/songs/1`
- Expected: song JSON or `404`

### D. Update song
- **Method**: `PUT`
- **URL**: `{{baseUrl}}/velasco/songs/1`
- **Body (raw JSON)**:
```json
{
  "title": "Shape of You (Updated)",
  "artist": "Ed Sheeran",
  "album": "Divide",
  "genre": "Pop",
  "url": "https://example.com/shape-of-you-updated"
}
```
- Expected: `200 OK` with updated object

### E. Search songs
- **Method**: `GET`
- **URL**: `{{baseUrl}}/velasco/songs/search/shape`
- Expected: matching list (possibly empty list)

### F. Delete song
- **Method**: `DELETE`
- **URL**: `{{baseUrl}}/velasco/songs/1`
- Expected: `200 OK` and delete message

## 6) Common Render issues
- **Connection refused / DB auth failed**:
  - Recheck `DB_USERNAME`, `DB_PASSWORD`, and `DATABASE_URL`
- **App boots but tables missing**:
  - Ensure `spring.jpa.hibernate.ddl-auto=update` in `application.properties`
- **503 first request**:
  - Free instances may sleep; retry after wake-up

## 7) Optional local run with same config style
Use your local PostgreSQL and keep these environment variables:
- `DATABASE_URL=jdbc:postgresql://localhost:5432/db_song`
- `DB_USERNAME=postgres`
- `DB_PASSWORD=admin`

Then run:
- `./mvnw spring-boot:run` (Linux/macOS)
- `mvnw.cmd spring-boot:run` (Windows)
