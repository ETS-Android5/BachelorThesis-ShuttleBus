const ERROR = require("../errors");
const db = require("./database.js");

function insertRoute(newData){
    if(newData.id != "undefined") delete newData.id;
    return db.collection("routes").add(newData)
    .then(()=>null,error=>{throw ERROR.server});
}

function getRouteById(id){
    return db.collection("routes").doc(id).get()
    .then((snapshot)=>{
        if(!snapshot.exists)
            return null;
        else{
            let origin = snapshot.data();
            origin.id = snapshot.id;
            return origin;
        };
    },error=>{throw ERROR.server});
}

function deleteRouteById(id){
    return db.collection("routes").doc(id).delete()
    .then(()=>null,error=>{throw ERROR.server});
}

function getRoutesByPostCode(postCode){
    return db.collection("routes").where("postcode","==",postCode).get()
    .then((snapshot) => {
        if(snapshot.docs.length > 0) return snapshot.docs.map(element=>{return element.data()});
        else return [];},
    (err)=>{throw ERROR.server })
}
module.exports = {
    deleteRouteById:deleteRouteById,
    insertRoute:insertRoute,
    getRouteById:getRouteById,
    getRoutesByPostCode:getRoutesByPostCode
}