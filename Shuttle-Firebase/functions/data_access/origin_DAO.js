    /**
     * 
     * @module data_access
     * 
     * @description Manager of the database. 
     *              In charge of communicating with the database to read and write origin data.
     * 
    */

    const ERROR = require("../errors/errors");
    const db = require("./database.js");

    function getAllOrigins() {

        return db.collection("origins").get()
        .then((snapshot) => {
            
            let origins = [];
            
            snapshot.docs.forEach((doc) => { origins.push({ name: doc.data().name, id: doc.id, coordinates: doc.data().coordinates}); })
            
            return origins;
            
        }, error => { throw ERROR.server });
    }

    function getOriginById(id) {

        return db.collection("origins").doc(id).get()
        .then((snapshot) => {

            if(!snapshot.exists) return null;
            else {

                let origin = snapshot.data();
                origin.id = snapshot.id;
                return origin;
            };

        }, error => { throw ERROR.server });
    }

    function getOriginByName(name) {

        return db.collection("origins").where("name","==",name).get()
        .then((snapshot) => {

            if(snapshot.docs.length > 0) {

                let origin = snapshot.docs[0].data();
                origin.id = snapshot.docs[0].id;

                return origin;
            }
            else return null;

        }, error => { throw ERROR.server });
    }

    function deleteOriginById(id) {

        return db.collection("origins").doc(id).delete()
        .then(() => null, error => { throw ERROR.server });
    }

    function modifyOriginById(id,newData){

        if(newData.id != "undefined") delete newData.id;

        return db.collection("origins").doc(id).set(newData)
        .then(() => null, error => { throw ERROR.server });
    }

    function insertOrigin(newData) {

        if(newData.id != "undefined") delete newData.id;

        return db.collection("origins").add(newData)
        .then((result) => result.id, error => { throw ERROR.server });
    }


    module.exports = { 

        getAllOrigins:getAllOrigins,
        getOriginById:getOriginById,
        deleteOriginById:deleteOriginById,
        modifyOriginById:modifyOriginById,
        insertOrigin:insertOrigin,
        getOriginByName:getOriginByName
    }
