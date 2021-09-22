<?php

class CApplication extends CComponent
{
    private $request = '';

    public function getRequest()
    {
        return $this->request;
    }

    public function setRequest($req)
    {
        $this->request = $req;
    }
}

class CComponent
{
    public function __get($name)
	{
		$getter='get'.$name;
		if(method_exists($this,$getter))
			return $this->$getter();
    }
}


?>