# Get the version from the debian changelog.
VERSION = ${shell expr "`head -1 ../debian/changelog`" : '.*(\(.*\))'}
TAG = $(shell echo -n "version_$(VERSION)" | tr -c -- "-_a-zA-Z0-9" "_")

DEBVERSION = ${shell expr "$(VERSION)" : '.*-\([0-9]\)'}
DEB = nice_${VERSION}_all.deb

BUILDROOT = ${CURDIR}/nice-$(VERSION)

BUILTSOURCEFILE = nice_$(VERSION).orig.tar.gz
TARFILE = Nice-${VERSION}-unix.tar.gz
SOURCEFILE = Nice-${VERSION}-source.tar.gz

ZIPFILE = Nice-${VERSION}-windows.zip
ZIP_ROOT = tmp/Nice

RPMTMP = tmp/Nice-${VERSION}
RPMDIR  = ${CURDIR}/tmp/noarch
RPMFILE = Nice-${VERSION}-1.noarch.rpm

CVSROOT=:pserver:anonymous@cvs.nice.sourceforge.net:/cvsroot/nice 

clean-tmp = rm -rf tmp && mkdir tmp

all: deb tar zip rpm

deb $(DEB):
	@echo Creating Debian package version ${VERSION}
	rm -rf $(BUILDROOT) $(BUILDROOT).orig
	cd .. && cvs tag -F $(TAG)
	CVSROOT=$(CVSROOT) cvs export -d $(BUILDROOT).orig -r $(TAG) Nice
	mv -f $(BUILDROOT).orig/debian $(BUILDROOT).orig-debian
	find $(BUILDROOT).orig -name '.cvsignore' | xargs rm
	cp ../share/java/nice.jar $(BUILDROOT).orig/external/nice-bootstrap.jar
	tar zcvf $(BUILTSOURCEFILE) $(BUILDROOT).orig
	mv $(BUILDROOT).orig-debian $(BUILDROOT).orig/debian
	mv $(BUILDROOT).orig $(BUILDROOT)
	cd $(BUILDROOT) && dpkg-buildpackage -us -uc -rfakeroot -sa


.PHONY: tar
tar ${TARFILE}: $(DEB)
	${clean-tmp}
	cd $(BUILDROOT) && $(MAKE) install PREFIX=${CURDIR}/tmp
	cd tmp && tar -czf ../${TARFILE} . --owner=0 --group=0

.PHONY: rpm
rpm ${RPMDIR}/${RPMFILE}: ${TARFILE}
	${clean-tmp}
	cp rpmmacros ~/.rpmmacros
	mkdir -p ${RPMTMP}/usr
	mkdir -p tmp/BUILD
	cd ${RPMTMP}/usr && tar xzf ${CURDIR}/${TARFILE}
	cd tmp && tar zcvf ${CURDIR}/tmp/Nice-${VERSION}.tar.gz Nice-${VERSION}
	sed "s/VERSION/${VERSION}/" Nice.spec > tmp/Nice-${VERSION}.spec
	rpmbuild --target=noarch --buildroot=${CURDIR}/${RPMTMP} -bb tmp/Nice-${VERSION}.spec


.PHONY: zip
zip ${ZIPFILE}: $(DEB)
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
         echo put ${DEB}; echo put ${BUILTSOURCEFILE} ${SOURCEFILE}; \
	 echo lcd ${RPMDIR}; echo put ${RPMFILE} \
	)\
        | ftp -n upload.sourceforge.net
	echo -e "Redirect /Wiki http://nice.sourceforge.net/cgi-bin/twiki/view" \
  "\nRedirect /Nice.tar http://prdownloads.sourceforge.net/nice/${TARFILE}" \
  "\nRedirect /Nice.zip http://prdownloads.sourceforge.net/nice/${ZIPFILE}" \
  "\nRedirect /nice.deb http://prdownloads.sourceforge.net/nice/${DEB}" \
  "\nRedirect /nice.rpm http://prdownloads.sourceforge.net/nice/${RPMFILE}" \
  > ../web/.htaccess

