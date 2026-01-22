import json
from sqlalchemy.orm import Session
from .models import Village, User, UserRole

def seed_data(db: Session):
    # Check if data exists
    if db.query(Village).first():
        return

    # Seed Village (Bihar -> Madhubani -> Benipatti -> Lakhnaur)
    # GeoJSON is a simplified polygon for demo
    geojson_data = {
        "type": "Polygon",
        "coordinates": [[
            [86.0, 26.0], [86.1, 26.0], [86.1, 26.1], [86.0, 26.1], [86.0, 26.0]
        ]]
    }

    village = Village(
        name="Lakhnaur",
        block="Benipatti",
        district="Madhubani",
        state="Bihar",
        boundary_geojson=geojson_data
    )
    db.add(village)
    db.commit()
    db.refresh(village)

    # Seed 5 Artists
    from passlib.context import CryptContext
    pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")
    password_hash = pwd_context.hash("password")

    artists = []
    for i in range(1, 6):
        artist = User(
            username=f"artist{i}",
            hashed_password=password_hash,
            full_name=f"Demo Artist {i}",
            village_id=village.id,
            role=UserRole.ARTIST.value,
            skill_badges={"kachni": True, "bharni": i % 2 == 0}, # Random skills
            trust_score=0.9 + (i * 0.01)
        )
        artists.append(artist)

    # Seed 1 Team Leader
    leader = User(
        username="leader1",
        hashed_password=password_hash,
        full_name="Team Leader One",
        village_id=village.id,
        role=UserRole.TEAM_LEADER.value
    )

    db.add_all(artists)
    db.add(leader)
    db.commit()
    print("Seeding complete.")
