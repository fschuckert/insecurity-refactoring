<?php
require_once("./Event.php");
require_once("./init.php");
require_once("./sink.php");

$db = new PDO('sqlite::memory:', null, null, array(PDO::ATTR_PERSISTENT => true));

$id = $_GET['id'];

$sql = "SELECT val FROM Tests WHERE id=$id";

Event::run('event', $data=$sql);




?>