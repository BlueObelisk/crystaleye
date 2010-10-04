== CRYSTALEYE ==

=== Installation ===

You will first need to download and install the following:

* A Java JDK (version 1.5 or later)
* Mercurial - http://mercurial.selenic.com/wiki/Download
* Maven 2 - http://maven.apache.org/download.html

Then:

1. Navigate to the directory you wish to download the CrystalEye code to and issue 
   the command:

		hg clone http://bitbucket.org/ned24/crystaleye
		
   this will create a 'crystaleye' dir.
2. Issue the following command from inside the 'crystaleye' dir:
 
		mvn package
		
   this will create a 'target' folder which contains (amongst other things) 
   crystaleye.jar
3. Copy crystaleye.jar to the place that you'll run it from.


=== Updating ===

* If you want to update your CrystalEye code, execute the following commands
  from your CrystalEye code directory:

		hg pull
		hg update
		
* Then you can do the steps 2 & 3 from the Installation section to install your
  updated CrystalEye.


=== Execution ===

* In the root of the 'crystaleye' directory that you cloned is a file called 
  'crystaleye.properties.example'.  Copy this to your preferred location on your
  machine and rename it to 'crystaleye.properties'.  If you are only going to be 
  using the crawlers and CIF2CML parts of CrystalEye then you only need to set the 
  cif.dir  and web.dir properties.  If you are using anything else, then you will 
  need to also set web.dir.url.

* To execute the CrystalEye code you need to issue a command of the type

		java -jar <path-to-your-crystaleye-jar> -p <path-to-your-properties-file>
		
* If you use the above command (with your machine specific paths put in) then all
  of the processes in CrystalEye will be run after one another, these are:

  -fetch : finds and downloads CIFs from publishers websites
  -cif2cml : converts CIFs to CML
  -cml2foo : creates 2D images of the structures and breaks them up into 
      moieties/fragments
  -webpage : creates the CrystalEye website around the structures that have been 
      downloaded
  -rss : creates a number of RSS feeds to publish the structures in your CrystalEye
  -doilist : keeps an index of all DOIs that have been downloaded
  -smiles : keeps an index of all SMILES contained within the structures.
  -cellparams : keeps an index of all the cell parameters of the structures.
  -bondlengths : keeps indexes of all the bond lengths found within the structures 
      and creates histograms of the data.
	
* If you just want to run one at a time, then you can append the name of the 
  processor as observed in the list above.  For instance, the following command
  would just run the web-crawlers
  
		java -jar <path-to-jar> -p <path-to-props> -fetch

  Or you could run the crawlers and CIF2CML processing with
  
		java -jar <path-to-jar> -p <path-to-props> -fetch -cif2cml 