const controller1 = {// can put comment

		/** @inject("templates/template1.html") */
		template : null,
		controller: ()=>{
		}
}
const controller2 = {
		/** @injectJS("http://localhost:8080/api") */
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
		/** @injectJS("http://localhost:8080/api") */
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
		/** @injectJS("http://localhost:8080/api") */
		template : [
			"asdf",
			"asdfasd"
			],
		controller: ()=>{
		}
}