<?php

include 'ex.php';

$untainted = "Hello";

add($_GET['lala'], $untainted);

add("Hello", "World");
?>
