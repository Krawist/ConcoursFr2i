package cm.seeds.concoursfr2i.Modeles

data class Personne(
    val nom : String,
    val isWoman : Boolean,
    val email : String,
    val telephone : String,
    val localisation : String,
    val age : Int,
    var isSynchronized: Boolean,
    var idUser : Long = 0)