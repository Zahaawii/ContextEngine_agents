FROM httpd:2.4-alpine

RUN sed -i 's/^Listen 80$/Listen 8081/' /usr/local/apache2/conf/httpd.conf

COPY . /usr/local/apache2/htdocs/

EXPOSE 8081
