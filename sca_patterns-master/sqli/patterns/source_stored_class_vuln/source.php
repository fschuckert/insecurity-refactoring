<?php

class Source
{
    private $parameter;

    private function getParameter($name)
    {
        return $this->parameter[$name];
    }

    public function setParameter($par)
    {
        $this->parameter = $par;
    }

    public function getParam($paramName, $default = null)
    {
        $value = $this->getParameter($paramName);
         if ((null === $value || '' === $value) && (null !== $default)) {
            $value = $default;
        }

        return $value;
    }
}

?>