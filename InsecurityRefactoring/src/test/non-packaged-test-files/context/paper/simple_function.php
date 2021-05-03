<?php
function getParam($param){
  return $_GET[$param];
}

function page($debug, $msg){
	$page = getParam('page');

	if(is_numeric($page)){
		$out = $msg . intval($page);
		$out = "<a href=\"www.url.com/" . $out . "\">";
	}
	else {
		$out = "Unknown page";
	}

	echo $out;
}
?>
