const controller1 = {// can put comment

		/** @injectAsString("templates/template1.html") */
		template : null,
		controller: ()=>{
		}
}
const controller2 = {
		/** @inject("http://localhost:8080/api") */
		template : {
			asdf : {
				asdf : "acc",
				asdd : 1
			}
		},
		controller: ()=>{
		}
}
const controller3 = {
		/** @inject("http://localhost:8080/api") */
		template : function(){
			var asdf = {asdf:"asdfasdf"};
			var ddd = function(){
				asdfasdf();
			}
		},
		controller: ()=>{
		}
}
const controller4 = {
		/** @inject("http://localhost:8080/api") */
		template : [
			"asdf",
			"asdfasd"
			],
		controller: ()=>{
		}
}

const controller5 = {
		/** @inject("http://localhost:8080/api") */
		template :  () => {
			
		},
		controller: ()=>{
		}
}


/** @inject("http://localhost:8080/api") */
let controller6  = () => {
	
}
/** @inject("http://localhost:8080/api") */
const controller7  = function(){
	
}
/** @inject("http://localhost:8080/api") */
var controller8 = 99;
