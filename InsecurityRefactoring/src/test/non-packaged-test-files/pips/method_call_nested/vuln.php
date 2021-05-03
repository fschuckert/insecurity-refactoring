<?php

require('foo.php');

$foo = new Foo();

$t3 = "Hello " . $foo->bar() . " here";

$t4 = "Sentence $t3 end";

$t = "first";
$t .= $t3;
$t .= "second";

echo $t3;



?>
