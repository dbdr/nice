# Get the version from the debian changelog.
FULL_DEB_VERSION = ${shell expr "`head -1 ../debian/changelog`" : '.*(\(.*\))'}
VERSION = ${shell expr "$(FULL_DEB_VERSION)" : '\(.*\)-[^-]*'}
DEBIAN_REVISION = ${shell expr "$(FULL_DEB_VERSION)" : '.*-\([^-]*\)'}

CVS_TAG = $(shell echo -n "version_$(VERSION)" | tr -c -- "-_a-zA-Z0-9" "_")

DEB = nice_${FULL_DEB_VERSION}_all.deb

BUILDROOT = nice-$(VERSION)
BUILDDIR = ${CURDIR}/$(BUILDROOT)

BUILTSOURCEFILE = nice_$(VERSION).orig.tar.gz
TARFILE = Nice-${VERSION}-unix.tar.gz
SOURCEFILE = Nice-${VERSION}-source.tar.gz

ZIPFILE = Nice-${VERSION}-windows.zip
ZIP_ROOT = tmp/Nice

RPMTMP  = ${CURDIR}/tmp/Nice-${VERSION}
RPMDIR  = ${CURDIR}/tmp/noarch
RPMFILE = Nice-${VERSION}-1.noarch.rpm

CVSROOT=:pserver:anonymous@cvs.nice.sourceforge.net:/cvsroot/nice 
DOWNLOADROOT = http://prdownloads.sourceforge.net/nice

clean-tmp = rm -rf tmp && mkdir tmp

all: deb tar zip rpm

deb $(DEB):
	@echo Creating Debian package version ${VERSION}
	rm -rf $(BUILDROOT) $(BUILDROOT).orig
	cd .. && cvs tag -d $(CVS_TAG) && cvs tag $(CVS_TAG)
	CVSROOT=$(CVSROOT) cvs export -d $(BUILDROOT).orig -r $(CVS_TAG) Nice
	mv -f $(BUILDROOT).orig/debian $(BUILDROOT).orig-debian
	find $(BUILDROOT).orig -name '.cvsignore' | xargs rm
	cp ../share/java/nice.jar $(BUILDROOT).orig/external/nice-bootstrap.jar
	tar zcf $(BUILTSOURCEFILE) $(BUILDROOT).orig
	mv $(BUILDROOT).orig-debian $(BUILDROOT).orig/debian
	mv $(BUILDROOT).orig $(BUILDROOT)
	cd $(BUILDROOT) && dpkg-buildpackage -us -uc -rfakeroot -sa


.PHONY: tar
tar ${TARFILE}: $(DEB)
	${clean-tmp} && mkdir tmp/$(BUILDROOT)
	cd $(BUILDROOT) && $(MAKE) install PREFIX=${CURDIR}/tmp/$(BUILDROOT)
	# Preprend a specific message for manual installations
	echo -e \
	 ';; Load this file from your .emacs file with:\n' \
	 ';;   (load "/usr/local/share/emacs/site-lisp/nice/nice-startup.el")'\
         '\n\n(setq load-path (cons "/usr/local/share/emacs/site-lisp/nice" load-path))\n\n' \
	> ${CURDIR}/tmp/$(BUILDROOT)/share/emacs/site-lisp/nice/nice-startup.el
	cat $(BUILDROOT)/lib/emacs/nice-startup.el \
	>>${CURDIR}/tmp/$(BUILDROOT)/share/emacs/site-lisp/nice/nice-startup.el
	cd tmp && tar -czf ../${TARFILE} $(BUILDROOT) --owner=0 --group=0

.PHONY: rpm
rpm ${RPMDIR}/${RPMFILE}: $(DEB)
	${clean-tmp}
	cp rpmmacros ~/.rpmmacros
	mkdir -p ${RPMTMP}
	mkdir -p tmp/BUILD
	cd $(BUILDROOT) && $(MAKE) install\
		PREFIX=${RPMTMP}/usr \
		LISPDIR=${RPMTMP}/usr/share/emacs/site-lisp
	cd tmp && tar zcf ${CURDIR}/tmp/Nice-${VERSION}.tar.gz Nice-${VERSION}
	sed "s/VERSION/${VERSION}/" Nice.spec > tmp/Nice-${VERSION}.spec
	rpmbuild --target=noarch --buildroot=${RPMTMP} -bb tmp/Nice-${VERSION}.spec


WIN_TEXT_FILES = ${BUILDDIR}/{bin/{nicec,nicedoc,niceunit,niceclasspath}.bat,web/Readme.txt}

.PHONY: zip
zip ${ZIPFILE}: $(DEB)
	${clean-tmp}
	mkdir ${ZIP_ROOT}
	../bin/nicec --man | groff -mandoc -Thtml - > ${ZIP_ROOT}/nicec.html
#	Make sure that the files are in DOS mode
	unix2dos -p ${WIN_TEXT_FILES} ${ZIP_ROOT}/nicec.html
	ln -sf ${WIN_TEXT_FILES} ${BUILDDIR}/share/java/nice.jar ${ZIP_ROOT}
	mkdir -p ${ZIP_ROOT}/Emacs
	ln -sf ${BUILDDIR}/lib/emacs/*.el ${ZIP_ROOT}/Emacs
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
  "\nRedirect /Nice.tar $(DOWNLOADROOT)/${TARFILE}" \
  "\nRedirect /Nice-source.tar.gz $(DOWNLOADROOT)/${SOURCEFILE}" \
  "\nRedirect /Nice.zip $(DOWNLOADROOT)/${ZIPFILE}" \
  "\nRedirect /nice.deb $(DOWNLOADROOT)/${DEB}" \
  "\nRedirect /nice.rpm $(DOWNLOADROOT)/${RPMFILE}" \
  > ../web/.htaccess

