The Guide is an OpenOffice .sxw file which is actually just a zip file.  
In order to open the Guide in OpenOffice, one must first change into 
the 'src/guide_vX.X/latest_date' directory and then zip the contents into a zip file
that has a .sxw extension.

Here is a command that uses the jar tool to zip the .sxw file ( Note: Make sure the 'M' is present and capitalized ):

jar -cvfM guide.sxw *


If any changes are made to the Guide, the .sxw file needs
to be unzipped before committing the changes to the CVS.

Again, the jar tool can be used to unzip the .sxw file:

jar -xvf guide.sxw
