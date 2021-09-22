<?php

$db = new PDO('sqlite::memory:', null, null, array(PDO::ATTR_PERSISTENT => true));
$db->query("CREATE TABLE Tests (id int, val varchar(255), CONSTRAINT PK_Test PRIMARY KEY (id));");

$db->query("INSERT INTO Tests VALUES (1, '1337');");
$db->query("INSERT INTO Tests VALUES (2, '2337');");
$db->query("INSERT INTO Tests VALUES (3, '3337');");


require_once('singleton.php');
require_once('url.php');

$global_instance = Singleton::getInstance();
$global_instance->url = new Url();

?>