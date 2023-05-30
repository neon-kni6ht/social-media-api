dropdb --if-exists social_media_api_db
    dropuser --if-exists sm_admin
    createuser sm_admin
    createdb -O sm_admin social_media_api_db
    psql -U sm_admin -d social_media_api_db <<EOF
GRANT ALL ON SCHEMA public TO sm_admin;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO sm_admin;
EOF