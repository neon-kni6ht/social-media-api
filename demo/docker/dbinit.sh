dropdb --if-exists social_media_api_db
    dropuser --if-exists admin
    createuser admin
    createdb -O admin social_media_api_db
    psql -U admin -d social_media_api_db <<EOF
GRANT ALL ON SCHEMA public TO admin;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO admin;
EOF