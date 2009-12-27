<?php 

class Repeater {
	
	private $message;
	
	function Repeater($message = null) {
		$this->message = $message;
	}
	
	function setMessage($message) {
		$this->message = $message;
	}
	
	function repeat() {
		return $this->message;
	}
	
}