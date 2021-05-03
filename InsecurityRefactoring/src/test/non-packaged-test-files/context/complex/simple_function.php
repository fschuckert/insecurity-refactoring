<?php
function getParam($param){
  return $_GET[$param];
}

function page($debug, $msg){
	$page = getParam('page');

	$b = "lulu";
	$c = "lala";
	$a = "Blubb" . $c;

	$post = $a . $b . "blubb";

	if(is_numeric($page)){
		$out = $msg . intval($page);
		$out = "<a href=\"www.url.com/" . $out . $post . "\">";
	}
	else {
		$out = "Unknown page";
	}

	echo $out;
}
?>
