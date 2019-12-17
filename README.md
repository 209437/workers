## Ta aplikacja pobiera pracownikow ze strony:

https://adm.edu.p.lodz.pl/user/users.php

i tworzy plik z VCard, który mozesz zaimportowac do swojego telefonu.

## Jak dziala aplikacja
Aplikacje nalezy uruchomic, oraz wejsc w przegladarce na adres http:
http://localhost:8080/getWorkers/{name}

na telefonie: {adres ip serwera}:8080/getWorkers/{name}

gdzie "name" jest imieniem lub nazwiskiem które chcemy wyszukać.

Na przyklad:
http://localhost:8080/getWorkers/paweł

Nastepnie znajdz plik w telefonie, z nazwą vCard_{nazwisko}.vcf i otwórz go. Wtedy mozesz zaimportowac kontakt do swoich kontaktów

