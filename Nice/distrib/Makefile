# Get the version from the debian changelog.
VERSION = ${shell expr "`head -1 ../debian/changelog`" : '.*(\(.*\))'}
DEB = nice_${VERSION}_all.deb
TARFILE = Nice-${VERSION}.tar
ZIPFILE = Nice-${VERSION}-windows.zip
CVSBUILDDIR = ${CURDIR}/tmp-deb
BUILDROOT = ${CVSBUILDDIR}/nice-${VERSION}
ZIP_ROOT = tmp/Nice
RPMTMP = tmp/Nice-${VERSION}
RPMDIR  = ${CURDIR}/tmp/noarch
RPMFILE = Nice-${VERSION}-1.noarch.rpm

clean-tmp = rm -rf tmp && mkdir tmp

all: deb tar zip rpm

deb $(CVSBUILDDIR)/$(DEB):
	@echo Creating Debian package version ${VERSION}
	cd .. && env CVSROOT=:pserver:anonymous@cvs.nice.sourceforge.net:/cvsroot/nice cvs-buildpackage -MNice -W${CVSBUILDDIR} -H${PWD}/prepare-bootstrap -us -uc -rfakeroot -F


.PHONY: tar
tar ${TARFILE}: $(CVSBUILDDIR)/$(DEB)
	${clean-tmp}
	cd $(CVSBUILDDIR)/nice-$(VERSION) && $(MAKE) install PREFIX=${CURDIR}/tmp
	cd tmp && tar -cf ../${TARFILE} . --owner=0 --group=0

.PHONY: rpm
rpm ${RPMDIR}/${RPMFILE}: ${TARFILE}
	${clean-tmp}
	cp rpmmacros ~/.rpmmacros
	mkdir -p ${RPMTMP}/usr
	mkdir -p tmp/BUILD
	cd ${RPMTMP}/usr && tar xvf ${CURDIR}/${TARFILE}
	cd tmp && tar zcvf ${CURDIR}/tmp/Nice-${VERSION}.tar.gz Nice-${VERSION}
	sed "s/VERSION/${VERSION}/" Nice.spec > tmp/Nice-${VERSION}.spec
	rpmbuild --target=noarch --buildroot=${CURDIR}/${RPMTMP} -bb tmp/Nice-${VERSION}.spec


.PHONY: zip
zip ${ZIPFILE}: $(CVSBUILDDIR)/$(DEB)
	${clean-tmp}
	mkdir ${ZIP_ROOT}
	../bin/nicec --man | groff -mandoc -Thtml - > ${ZIP_ROOT}/nicec.html
	ln -sf ${BUILDROOT}/bin/nicec.bat ${BUILDROOT}/share/java/nice.jar ${BUILDROOT}/web/Readme.txt ${ZIP_ROOT}
	mkdir -p ${ZIP_ROOT}/Emacs
	ln -sf ${BUILDROOT}/lib/emacs/*.el ${ZIP_ROOT}/Emacs
	-rm ${ZIPFILE}
	cd tmp && zip -r ../${ZIPFILE} Nice

#### SEND

send: ${TARFILE} ${ZIPFILE} ${RPMDIR}/${RPMFILE}
	(echo user anonymous bonniot@users.sf.net; \
         echo binary; echo cd incoming; \
         echo put ${TARFILE}; echo put ${ZIPFILE}; \
         echo lcd ${CVSBUILDDIR}; echo put ${DEB}; \
	 echo lcd ${RPMDIR}; echo put ${RPMFILE} \
	)\
        | ftp -n upload.sourceforge.net
	echo -e "Redirect /Wiki http://nice.sourceforge.net/cgi-bin/twiki/view" \
  "\nRedirect /Nice.tar http://prdownloads.sourceforge.net/nice/${TARFILE}" \
  "\nRedirect /Nice.zip http://prdownloads.sourceforge.net/nice/${ZIPFILE}" \
  "\nRedirect /nice.deb http://prdownloads.sourceforge.net/nice/${DEB}" \
  "\nRedirect /nice.rpm http://prdownloads.sourceforge.net/nice/${RPMFILE}" \
  > ../web/.htaccess

