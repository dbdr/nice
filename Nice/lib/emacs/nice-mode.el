;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;
;;;                    Major mode for editing Nice programs
;;;                                                        
;;;              Based on Jazz mode (Francois.Bourdoncle@ensmp.fr)
;;;       Adaptation to Nice by Daniel Bonniot (d.bonniot@mail.dotcom.fr)
;;;                                                        
;;;                                                        
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(require 'cc-vars)
(require 'cc-engine)
(require 'cc-mode)
(require 'cc-cmds)
(require 'cc-align)
(require 'cc-styles)
(require 'cc-defs)
(require 'cc-menus)
(require 'cc-defs)
(require 'cc-langs)

(defvar nice-program "nicec"
  "Nice compiler name.")

(defvar nice-xprogram nil
  "Nice compiler name plus arguments")

(defvar nice-mode-hook nil
  "Nice mode hook.")

(defvar nice-mode-syntax-table nil
  "Syntax table used in Nice buffers.")

(defvar nice-mode-map nil
  "Keymap used in Nice buffers.")

(defvar nice-variable-stack nil)
(defvar nice-directory nil)
(defvar nice-process nil)
(defvar nice-recompile-flag nil)
(defvar nice-recompile-all-flag nil)
(defvar nice-last-location 1)
(defconst nice-extension ".nice")
(defconst nice-extension-regexp "\\.nice$")

(defvar nice-export-directory "."
  "Export directory for HTML version of Nice source files.")

(defvar nice-output-directory "."
  "Export directory for HTML version of Nice outputs.")

(defvar nice-file-location-regexp
  "\"\\([^\"]+\\)\":\\([0-9]+\\)\\.\\([0-9]+\\)-\\([0-9]+\\)\\.\\([0-9]+\\)")

(defvar nice-loc-regexp
  "\\([0-9]+\\):\\([0-9]+\\)\\.\\([0-9]+\\)-\\([0-9]+\\)\\.\\([0-9]+\\)")

(defvar nice-mode-keywords
  (list
   ;; Class declaration
   '("\\<\\(class\\|interface\\)\\>\\s-+\\<\\(\\w+\\)\\>"
     2 nice-declaration-face)
   
   ;; Package declaration
   '("\\<package\\s-+\\([\\.a-zA-Z0-9_]+\\)"
     1 (progn
         (nice-link-create
          (match-beginning 1) (match-end 1)
          (cons 'package (match-string-no-properties 1))
          'nice-link-declaration-face)
         nice-declaration-face))
   
   ;; Object import
   '("^\\s-*import\\s-+\\([*\\.a-zA-Z0-9_]+\\)"
     1 (nice-link-create
        (match-beginning 1) (match-end 1)
        (cons 'symbol (match-string-no-properties 1))
        'nice-link-face))
   
   ;; Package import
   '("^\\s-*require\\s-+\\([\\.a-zA-Z0-9_]+\\)"
     1 (nice-link-create
        (match-beginning 1) (match-end 1)
        (cons 'package (match-string-no-properties 1))
        'nice-link-face))
   
   ;; Method declaration
   '("^\\s-*\\(\\<\\(static\\|public\\|fun\\|final\\|native\\)\\>\\s-+\\)*\\(\\<\\w+\\>\\.\\)?\\(\\<\\w+\\>\\)\\s-*\\(<\\(\\w\\|\\s-\\|[,:]\\)*>\\)?\\s-*\\(@\\s-*\\w+\\(<\\(\\w\\|\\s-\\|[,:]\\)*>\\)?\\)?\\s-*([^#!@%&\\*\\+-/<>=\\^|~]"
     4 nice-declaration-face)
   
   ;; Bogus method declaration
   '("\\<\\(native\\)\\s-*("
     1 nil t)
   
   ;; Bogus method declaration
   '("[-?:,=*+/)(]\\s-*
+\\s-*\\(\\<[a-z][A-Z_a-z0-9']*\\>\\.\\)?\\<\\(\\w+\\)\\>\\s-*("
     2 nil t)
   
   ;; Method declaration after comments
   '("^\\s-*//.*
+\\s-*\\<\\(\\w+\\)\\>\\s-*("
     1 nice-declaration-face t)
   
   ;; Field declaration
   '("^\\s-*\\(\\<\\(static\\|input\\|output\\|public\\|dynamic\\)\\>\\s-+\\)*\\<\\(\\w+\\)\\>\\s-*:.*[=;]"
     3 nice-declaration-face)
   
   ;; Variable declaration
   '("^\\s-*\\<var\\>\\s-+\\<\\(\\w+\\)\\>\\s-*\\(:\\s-*[^=;]*\\)?\\s-*[;=]"
     1 nice-declaration-face)
   
   ;; Bogus field declaration
   '("[-?:,=*+/)(]\\s-*
+\\s-*\\<\\(\\w+\\)\\>\\s-*:"
     1 nil t)
   
   ;; Field declaration after comments
   '("^\\s-*//.*
+\\s-*\\<\\(\\w+\\)\\>\\s-*:"
     1 nice-declaration-face t)
   
   ;; Constants
   '("\\<\\(true\\|false\\|this\\|null\\|[A-Z][A-Z_]+\\)\\>"
     1 nice-constant-face)
   
   ;; Classes
   '("[^\\.]\\<\\(_?[A-Z][a-zA-Z][a-zA-Z_'0-9]*\\|void\\|double\\|float\\|long\\|int\\|char\\|short\\|byte\\|boolean\\)\\>"
     1 nice-type-face)
   
   ;; Classes
   '("^\\<\\(_?[A-Z][a-zA-Z][a-zA-Z_'0-9]*\\|void\\|double\\|float\\|long\\|int\\|char\\|short\\|byte\\|boolean\\)\\>"
     1 nice-type-face)
   
   ;; Field definition
   '("^\\s-*_?[A-Z][a-zA-Z_][a-zA-Z_'0-9]*\\.\\(\\w+\\)\\s-*="
     1 nice-declaration-face)
   
   ;; Keywords
   '("\\<\\(pragma\\|fun\\|print\\|error\\|static\\|final\\|extends\\|implements\\|abstract\\|abstracts\\|public\\|var\\|dynamic\\|class\\|interface\\|device\\|new\\|device\\|as\\|export\\|instanceof\\|for\\|if\\|else\\|input\\|output\\|div\\|native\\|extern\\|reset\\|enable\\|import\\|require\\|package\\|alike\\|try\\|catch\\|finally\\|throw\\)\\>\\|@"
     0 nice-keyword-face)
   
   ;; For/if/assert
   '("^\\s-*\\(dynamic\\|assert\\|for\\|reset\\|enable\\|if\\)\\s-*("
     1 nice-keyword-face t))
  "Nice mode keywords.")

(defvar cc-imenu-nice-generic-expression
  (`
   ((nil
     (,
      (concat
       "^\\([ \t]\\)*"
       "\\([A-Za-z0-9_-]+[ \t]+\\)?"    ; type specs; there can be
       "\\([A-Za-z0-9_-]+[ \t]+\\)?"    ; more than 3 tokens, right?
       "\\([A-Za-z0-9_-]+[ \t]*[[]?[]]?\\)"
       "\\([ \t]\\)"
       "\\([A-Za-z0-9_-]+\\)"           ; the string we want to get
       "\\([ \t]*\\)+("
       "\\([a-zA-Z,_1-9\n \t]*[[]?[]]?\\)*" ; arguments
       ")[ \t]*"
       "[^;(]"
       "[,a-zA-Z_1-9\n \t]*{"               
       )) 6)))
  "Imenu generic expression for Nice mode.  See `imenu-generic-expression'.")

;; Face used to select statements/expressions
(make-face 'nice-selection-face)
(set-face-background 'nice-selection-face "#cccccc")
(set-face-foreground 'nice-selection-face "black")

;; Face used to emphasize keywords
(make-face 'nice-keyword-face)
(set-face-foreground 'nice-keyword-face "#0000ee")
(defconst nice-keyword-face 'nice-keyword-face)

;; Face used to emphasize constants
(make-face 'nice-constant-face)
(set-face-foreground 'nice-constant-face "#8080ff")
(defconst nice-constant-face 'nice-constant-face)

;; Face used to emphasize string constants
(make-face 'nice-string-face)
(set-face-foreground 'nice-string-face "#a020f0")
(defconst nice-string-face 'nice-string-face)

;; Face used to emphasize declarations
(make-face 'nice-declaration-face)
(set-face-foreground 'nice-declaration-face "#cd0000")
(defconst nice-declaration-face 'nice-declaration-face)

;; Face used to emphasize types
(make-face 'nice-type-face)
(set-face-foreground 'nice-type-face "#b7860b")
(defconst nice-type-face 'nice-type-face)

;; Face used to emphasize comments
(make-face 'nice-comment-face)
(set-face-foreground 'nice-comment-face "#008b00")
(defconst nice-comment-face 'nice-comment-face)

;; Face used to emphasize links
(make-face 'nice-link-face)
(set-face-foreground 'nice-link-face "#000000")
(set-face-underline-p 'nice-link-face t)
(defconst nice-link-face 'nice-link-face)

;; Face used to emphasize declaration links
(make-face 'nice-link-declaration-face)
(set-face-foreground 'nice-link-declaration-face "#cd0000")
(set-face-underline-p 'nice-link-declaration-face t)
(defconst nice-link-declaration-face 'nice-link-declaration-face)

;; System-dependant path separator
(defconst nice-path-separator
  (if (eq window-system 'w32) ";" ":"))

;; Syntax table initialization
(if nice-mode-syntax-table
    ()
  (setq nice-mode-syntax-table (make-syntax-table))
  (c-populate-syntax-table nice-mode-syntax-table)
  (modify-syntax-entry ?_ "w" nice-mode-syntax-table)
  ;(modify-syntax-entry ?\' "w" nice-mode-syntax-table)
  )

;; Local keymap initialization

(if nice-mode-map
    ()
  (setq nice-mode-map (make-sparse-keymap))
  (define-key nice-mode-map [menu-bar] (make-sparse-keymap))
  (define-key nice-mode-map [menu-bar c]
    (cons "Nice" (make-sparse-keymap "Nice")))
  (define-key nice-mode-map [menu-bar c nice-toggle-recompile-all]
    '("Toggle force-recompile-all mode" . nice-toggle-recompile-all))
  (define-key nice-mode-map [menu-bar c nice-toggle-recompile]
    '("Toggle force-recompile mode" . nice-toggle-recompile))
  (define-key nice-mode-map [menu-bar c nice-html-export]
    '("Generate HTML listing" . nice-html-export))
  (define-key nice-mode-map [menu-bar c nice-latex-export]
    '("Generate LaTeX listing" . nice-latex-export))
  (define-key nice-mode-map [menu-bar c nice-display-package]
    '("Display package" . nice-display-package))
  (define-key nice-mode-map [menu-bar c nice-uncomment-region-or-line]
    '("Uncomment region/line" . nice-uncomment-region-or-line))
  (define-key nice-mode-map [menu-bar c nice-comment-region-or-line]
    '("Comment region/line" . nice-comment-region-or-line))
  (define-key nice-mode-map [menu-bar c nice-next-error]
    '("Next error" . nice-next-error))
  (define-key nice-mode-map [menu-bar c nice-compile-buffer]
    '("Compile buffer" . nice-compile-buffer)))

;; Nice syntactic constructs
(defconst c-Nice-conditional-key
  "\\b\\(for\\|if\\|do\\|else\\|while\\|switch\\)\\b[^_]")

(defconst c-Nice-comment-start-regexp "/\\(/\\|[*][*]?\\)")

(defconst c-Nice-class-key
  (concat
   "\\(" c-protection-key "\\s +\\)?"
   "\\(interface\\|class\\|device\\)\\s +"
   c-symbol-key                         ;name of the class
   "\\(\\s *extends\\s *" c-symbol-key "\\)?" ;maybe followed by superclass 
   "\\(\\s *implements *[^{]+{\\)?"     ;maybe the adopted protocols list
   ))

(defconst c-Nice-method-key
  (concat
   "^\\s *[+-]\\s *"
   "\\(([^)]*)\\)?"			; return type
   ;; \\s- in nice syntax table does not include \n
   ;; since it is considered the end of //-comments.
   "[ \t\n]*" c-symbol-key))

(defconst c-Nice-access-key nil)

(defun nice-mode ()
  "
Mode for editing/compiling Nice programs.

* PROGRAMS CAN BE EDITED WITH THE FOLLOWING COMMANDS:

  Key                     Command
  ---                     -------
  
  Tab                     indent-for-tab-command
  Help                    nice-next-error
  C-c C-n                 nice-next-error
  Do                      nice-compile-buffer
  C-c C-b                 nice-compile-buffer
  C-c C-h                 nice-html-export
  C-c C-l                 nice-latex-export
  C-c C-p                 nice-display-package
  C-c C-c                 nice-comment-region-or-line
  C-c C-u                 nice-uncomment-region-or-line
  C-c p                   nice-toggle-pretty-print
  C-c r                   nice-toggle-recompile
  C-c R                   nice-toggle-recompile-all

* NET LISTS CAN BE INSPECTED WITH THE FOLLOWING COMMANDS:

  Key                     Command
  ---                     -------
  
  Tab                     nice-find-variable
  S-Tab                   nice-previous-location
  Space                   nice-locate

* THE MODE IS CONTROLED BY THE FOLLOWING VARIABLES:

  nice-program            Name of nice compiler (default = \"nicec\")
  nice-mode-hook          User mode hook called after initialization

* COLORS AND FONTS CAN BE CUSTOMIZED WITH THE FOLLOWING FACES:

  Faces
  -----

  nice-selection-face     Face used to select source code
  nice-keyword-face       Face used to emphasize keywords
  nice-constant-face      Face used to emphasize constants
  nice-declaration-face   Face used to emphasize declarations
  nice-string-face        Face used to emphasize string constants
  nice-type-face          Face used to emphasize types
  nice-comment-face       Face used to emphasize comments
  nice-link-face          Face used to highlight links
  nice-link-declaration-face Face used to highlight declaration links"

  (interactive)
  (c-initialize-cc-mode)
  (kill-all-local-variables)
  (set-syntax-table nice-mode-syntax-table)
  (setq major-mode 'nice-mode
 	mode-name "Nice"
 	local-abbrev-table java-mode-abbrev-table)
  (use-local-map nice-mode-map)
  (c-common-init)
  (setq comment-start "// "
 	comment-end   ""
 	c-conditional-key c-Nice-conditional-key
 	c-comment-start-regexp c-Nice-comment-start-regexp
  	c-class-key c-Nice-class-key
	c-method-key c-Nice-method-key
 	c-baseclass-key nil
	c-recognize-knr-p nil
 	c-access-key c-Nice-access-key
        c-label-key "###"
	imenu-generic-expression cc-imenu-nice-generic-expression)
  (c-add-style
   "nice"
   '((c-basic-offset . 2)
     (c-comment-only-line-offset 0 . 0)
     (c-hanging-comment-starter-p)
     (c-offsets-alist
      (topmost-intro-cont . +)
      (statement-block-intro . +)
      (knr-argdecl-intro . 5)
      (substatement-open . +)
      (label . +)
      (statement-case-open . +)
      (statement-cont . nice-lineup-runin-statements)
      (arglist-intro . c-lineup-arglist-intro-after-paren)
      (arglist-close . c-lineup-arglist)
      (arglist-cont . c-lineup-arglist-intro-after-paren)
      (access-label . +)
      (inher-cont . c-lineup-java-inher)
      (func-decl-cont . c-lineup-java-throws))) t)
  (local-set-key "\C-c\C-b" 'nice-compile-buffer)
  (local-set-key "\C-c\C-c" 'nice-comment-region-or-line)
  (local-set-key "\C-c\C-u" 'nice-uncomment-region-or-line)
  (local-set-key "\C-c\C-h" 'nice-html-export)
  (local-set-key "\C-c\C-l" 'nice-latex-export)
  (local-set-key "\C-c\C-p" 'nice-display-package)
  (local-set-key "\C-c\C-n" 'nice-next-error)
  (local-set-key "\C-cr" 'nice-toggle-recompile)
  (local-set-key "\C-cR" 'nice-toggle-recompile-all)
  (local-set-key "\C-l" 'nice-fontify-buffer)
  (local-set-key "{" 'c-electric-brace)
  (local-set-key "}" 'c-electric-brace)
  (local-set-key [menu] 'nice-compile-buffer)
  (local-set-key [mouse-1] 'nice-link-activate)
  (local-set-key [down-mouse-3] 'nice-files-menu)
  (local-set-key [help] 'nice-next-error)
  (make-local-variable 'font-lock-defaults)
  (setq font-lock-defaults
        '(nice-mode-keywords))
  (make-local-variable 'font-lock-comment-face)
  (setq font-lock-comment-face 'nice-comment-face)
  (make-local-variable 'font-lock-string-face)
  (setq font-lock-string-face 'nice-string-face)
  (turn-on-font-lock)
  (setq buffer-modified-p nil)
  (let ((name (buffer-file-name)))
    (when name
      (save-excursion
        (goto-char (point-min))
        (cond
         ((re-search-forward
           "^package\\s-+\\([\\.a-zA-Z0-9_]*\\)\\s-*[{;]" (point-max) t)
          (nice-rename-buffer (concat (match-string-no-properties 1) "."
                                      (file-name-sans-extension
                                       (file-name-nondirectory
                                        (buffer-file-name))))))
         ((nice-file-p name)
          (nice-rename-buffer (file-name-nondirectory
                               (substring name 0 (- (length name) (length nice-extension))))))))))
  (run-hooks 'nice-mode-hook)
  (c-update-modeline))

(defun nice-lineup-runin-statements (langelem)
  ;; line up statements in coding standards which place the first
  ;; statement on the same line as the block opening brace.
  (let ((char  (cdr langelem)))
    (save-excursion
      (goto-char (cdr langelem))
      (if (looking-at ".*=$") 2 0))))

(defun nice-buffer-pkg-name ()
  (save-excursion
    (goto-char (point-min))
    (let ((start (search-forward "package " nil t))
	  (end (search-forward ";")))
      (if start
	  (buffer-substring start (- end 1))
	(error "\"package\" statement not found"))
    )
  )
)

(defun nice-compile-buffer ()
  "Compile a nice buffer."
  (interactive)
  (let (cmd)
    (setq nice-last-location 1)
    (setq nice-variable-stack nil)
    (if nice-process
        (delete-process nice-process))
    (if (get-buffer "*Nice*")
        (kill-buffer "*Nice*"))
    (save-buffer)

    ;; Build process command
    (setq cmd
          (append
           (if nice-xprogram nice-xprogram (cons nice-program nil))
           (if nice-recompile-flag '("-r"))
           (if nice-recompile-all-flag '("-R"))
           (cons (nice-buffer-pkg-name)
                 nil)))
    
    ;; Save the current directory
    (setq nice-directory (file-name-directory (buffer-file-name)))

    ;; Start process
    (setq nice-process (nice-start-process nice-directory cmd))

    ;; User feedback
    (setq nice-compiling t)
    
    ;; Bindings
    (let ((name (nice-buffer-name (current-buffer))))
      (save-excursion
        (set-buffer "*Nice*")
        (set (make-variable-buffer-local 'nice-output-name) name)
        (delete-region (point-min) (point-max))
        (local-set-key " " 'nice-find-variable)
        (local-set-key [S-tab] 'nice-previous-location)
        (local-set-key "\C-c\C-e" 'nice-export-output)
        (local-set-key " " 'nice-locate)))

    ;; Display the compilation buffer
    (display-buffer "*Nice*")))

(defun nice-next-error ()
  "Find next Nice error."
  (interactive)
  (let ((buffer (get-buffer "*Nice*"))
        start file l1 c1 l2 c3)
    (if (not buffer)
        (message "Error: buffer not compiled." (beep))
      (if nice-last-location
          (save-excursion
            (set-buffer buffer)
            (setq mark-active nil)
            (goto-char nice-last-location)
            (if (setq nice-last-location
                      (nice-search-forward nice-file-location-regexp
                                           (point-max)))
                (progn
                  (setq start (match-beginning 1))
                  (setq file (match-string-no-properties 1))
                  (setq l1 (nice-match-to-int 2))
                  (setq c1 (nice-match-to-int 3))
                  (setq l2 (nice-match-to-int 4))
                  (setq c2 (nice-match-to-int 5)))
              (nice-no-more-errors)))
        (nice-no-more-errors))
      (if (and file l1 c1 l2 c2)
          (progn
            (nice-select-file file nil l1 c1 l2 c2)
            (set-window-start (display-buffer buffer)
                              (save-excursion
                                (set-buffer buffer)
                                (goto-char start)
                                (beginning-of-line)
                                (point))))
        (nice-clear)))))

(cond
 ;; Hilighting for XEmacs
 ((string-match "XEmacs" emacs-version) 
  (defun match-string-no-properties (n) 
    (match-string n)) 
  (defvar nice-selection-extent (make-extent 1 1)) 
  
  (defun nice-select (window start end)  
    (set-extent-endpoints  
     nice-selection-extent  
     start  
     end  
     (window-buffer window)) 
    (if (not pre-command-hook) 
        (set-extent-face nice-selection-extent 'nice-selection-face)) 
    (set-window-point window start) 
    (setq pre-command-hook 'nice-clear)) 
  
  (defun nice-clear () 
    (set-extent-face nice-selection-extent 'default) 
    (setq pre-command-hook nil)))

 ;; Hilighting for GNU Emacs
 (t 
  (defvar nice-overlay (make-overlay 1 1)) 
  (defun nice-clear () 
    (overlay-put nice-overlay 'face 'default) 
    (setq pre-command-hook nil)) 
  
  
  (defun nice-select (window start end) 
    (move-overlay nice-overlay start end (window-buffer window)) 
    (if (not pre-command-hook) 
        (overlay-put nice-overlay 'face 'nice-selection-face)) 
    (set-window-point window start) 
    (setq pre-command-hook 'nice-clear))))

(defun nice-newline ()
  "Indent the line, then insert a newline."
  (interactive)
  (indent-for-tab-command)
  (newline-and-indent))

(defun nice-toggle-recompile ()
  "Disable/enable recompile mode."
  (interactive)
  (setq nice-recompile-flag (not nice-recompile-flag))
  (message "Forced recompilation %s." (if nice-recompile-flag "on" "off")))

(defun nice-toggle-recompile-all ()
  "Disable/enable the recompile-all mode."
  (interactive)
  (setq nice-recompile-all-flag (not nice-recompile-all-flag))
  (message "Forced recompilation of all packages %s." (if nice-recompile-all-flag "on" "off")))

;; Return the package of a object (nil for global objects)
(defun nice-object-package (object)
  (let ((package
         (mapconcat 'identity
                    (reverse
                     (cdr
                      (reverse
                       (split-string object "\\.")))) ".")))
    (if (string-equal package "") nil package)))

;; Return the relative path of an object
(defun nice-object-directory (object)
  (let ((package
         (mapconcat 'identity
                    (reverse
                     (cdr
                      (reverse
                       (split-string object "\\.")))) "/")))
    (if (string-equal package "") "." package)))

;; Return the object without its package qualification
(defun nice-object-name (object)
  (car (last (split-string object "\\."))))

;; Find the object "oname" of the kind "kind" in package "opackage" (directory
;; "odir" is the relative path corresponding to package "opackage")
(defun nice-find-object-in-dir (dir odir kind opackage oname)
  (let ((object (concat dir "/" odir "/" oname)))
    (cond
     ((and (eq kind 'package)
           (nice-package-p object))
      (list 'package opackage oname object))
     ((and (eq kind 'file)
           (nice-file-p (concat object nice-extension)))
      (list 'file opackage oname (concat object nice-extension)))
     ((eq kind 'symbol)
      (or
       ;; Try to locate a file with the name of the symbol
       (nice-find-object-in-dir dir odir 'file opackage oname)
       
       ;; Try to locate a file with the name of the symbol in lowercase
       (nice-find-object-in-dir dir odir 'file opackage (downcase oname))
       
       ;; Otherwise display the package of the symbol
       (and opackage
            (nice-find-object-in-dir dir
                                     (file-name-directory odir)
                                     'package
                                     (nice-object-package opackage)
                                     (nice-object-name opackage)))))
     (t nil))))

;; Find the object of the given kind (file/package/symbol)
(defun nice-find-object (kind object)
  (let* ((opackage (nice-object-package object))
         (odir (nice-object-directory object))
         (oname (nice-object-name object))
         (path (cons "." (split-string (or (getenv "JAZZPATH") ".")
                                       nice-path-separator))))
    (catch 'found
      (while path
        (let ((job (nice-find-object-in-dir
                    (car path) odir kind opackage oname)))
          (if job (throw 'found job))
          (setq path (cdr path)))))))
  
;; Create a link
(defun nice-link-create (beg end link face)
  (when (nice-find-object (car link) (cdr link))
    (put-text-property beg end 'mouse-face face)
    (put-text-property beg end 'front-nonsticky t)
    (put-text-property beg end 'rear-nonsticky t)
    (put-text-property beg end 'nice-link link)
    nil))

;; Activate a link
(defun nice-link-activate ()
  (interactive)
  (deactivate-mark)
  (let* ((link (get-text-property (point) 'nice-link)))
    (when link (nice-display-object (car link) (cdr link) nil))))

;; Display a given package
(defun nice-display-package (package)
  "Display a Nice package."
  (interactive "sPackage: ")
  (nice-display-object 'package package nil))

;; Returns true if "dir" is a Nice package
(defun nice-package-p (dir)
  (let* ((name (file-name-nondirectory dir))
         (dir (concat dir "/.nice")))
    (and (not (string-equal name "."))
         (not (string-equal name ".."))
         (file-directory-p dir))))

;; Returns true if "file" is a nice source file
(defun nice-file-p (file)
  (and file
       (file-readable-p file)
       (string-match nice-extension-regexp file)))

;; Make the specified object of the given kind (file/package/symbol)
;; visible in some window
(defun nice-display-object (kind object export)
  (let* ((job (nice-find-object kind object))
         (kind (and job (nth 0 job)))
         (opackage (and job (nth 1 job)))
         (oname (and job (nth 2 job)))
         (opath (and job (nth 3 job))))
    (cond
     ;; Package
     ((eq kind 'package)
      (let* ((allfiles (directory-files opath t nil nil))
             (package (if opackage (concat opackage "." oname) oname))
             (buffer "*Nice package*")
             beg end files state)
        ;; Create a temporary buffer
        (with-output-to-temp-buffer buffer
          (save-excursion
            (set-buffer buffer)
            (toggle-read-only -1)
            (setq beg (point))
            (princ "package ")
            (put-text-property beg (1- (point)) 'face nice-keyword-face)
            (setq beg (point))
            (if (not package)
                (princ "root")
              (let ((p (nice-object-package package))
                    (n (nice-object-name package)))
                (if (not p)
                    (princ package)
                  (princ p)
                  (nice-link-create beg (point)
                                    (cons 'package p)
                                    'nice-link-declaration-face)
                  (princ ".")
                  (princ n))))
            (setq end (point))
            (princ " {\n")
            (put-text-property beg end 'face nice-declaration-face)
            (setq state 0)
            (while (< state 2)
              (setq files allfiles)
              (while files
                (let* ((file (and (file-readable-p (car files)) (car files)))
                       (name (and file (file-name-nondirectory file)))
                       (ispackage (and file (nice-package-p file)))
                       (isfile (and file (nice-file-p file))))
                  (when (or (and ispackage (= state 0))
                            (and isfile (= state 1)))
                    (princ "  ")
                    (setq beg (point))
                    (princ (if isfile "file" "package"))
                    (princ " ")
                    (put-text-property beg (1- (point))
                                       'face nice-keyword-face)
                    (setq beg (point))
                    (if ispackage
                        (when package
                          (princ package)
                          (princ ".")))
                    (let ((nm
                           (if ispackage name
                             (substring name 0 (- (length name) 4)))))
                      (princ nm)
                      (nice-link-create
                       beg (point)
                       (cons (if ispackage 'package 'file)
                             (concat (if package
                                         (concat package ".") "") nm))
                       'nice-link-face))
                    (princ ";\n"))
                  (setq files (cdr files))))
              (setq state (1+ state)))
            (princ "}")))
        
        ;; Bindings for the temporary buffer
        (let ((name (concat
                     "package."
                     (if (not package) "root"
                       (let ((p (nice-object-package package))
                             (n (nice-object-name package)))
                         (if (not p) package (concat p "." n)))))))
          (save-excursion
            (set-buffer buffer)
            (if (get-buffer name)
                (kill-buffer name))
            (nice-rename-buffer name)
            (local-set-key [mouse-1] 'nice-link-activate)
            (local-set-key "\C-c\C-h" 'nice-html-export)
            (local-set-key "\C-c\C-l" 'nice-latex-export)
            (local-set-key "\C-c\C-e" 'nice-export-package)
            (when export (nice-export ""))))))

     ;; Source file
     ((eq kind 'file)
      (let ((window (display-buffer (find-file-noselect opath t nil))))
        (save-excursion
          (set-buffer (window-buffer window))
          (goto-char (point-min))
          (let ((case-fold-search t))
            (when (re-search-forward (concat "^\\s-*\\<public\\>.*\\(\\<"
                                             oname "\\>\\)")
                                     (point-max) t nil)
              (nice-select window (match-beginning 1)
                           (match-end 1)))))))
     (t
      (message "Object \"%s\" not found." object)))))

(defun nice-rename-buffer (name)
  (if (get-buffer name)
      (let ((n 2) tmp)
        (while (get-buffer (setq tmp (concat name "<" n ">")))
          (setq n (+ n 1)))
        (setq name tmp)))
  (rename-buffer name))

(defun nice-buffer-name (buffer)
  (let ((name (buffer-name buffer)))
    (when name
      (if (string-match "<[0-9]+>$" name)
          (substring name 0 (match-beginning 0))
        name))))

(defun nice-files-menu (event)
  "Pop up a menu of Nice modules for selection with the mouse."
  (interactive "e")
  (let ((buffer-list (buffer-list))
        entry-list buffers menu)
    ;; Opened modules
    (while buffer-list
      (let* ((buffer (car buffer-list))
             (file (buffer-file-name buffer)))
        (cond
         ((nice-file-p file)
          (setq entry-list (cons (cons (buffer-name buffer) buffer)
                                 entry-list)))
         ((string-match "^package\\." (buffer-name buffer))
          (setq entry-list (cons (cons (buffer-name buffer) buffer)
                                 entry-list))))
        (setq buffer-list (cdr buffer-list))))
    ;; Sort the menu
    (setq entry-list
          (sort entry-list
                '(lambda (x y)
                   (string-lessp (car x) (car y)))))
    ;; Display the menu
    (if (> (length entry-list) 0)
        (let* ((menu (list "Nice files" (cons "" entry-list)))
               (buf (x-popup-menu event menu))
               (window (posn-window (event-start event))))
          (when buf
            (or (framep window) (select-window window))
            (switch-to-buffer buf))))))

(defun nice-previous-location ()
  "Jump back to the initial location after a nice-find-variable."
  (interactive)
  (if (not nice-variable-stack)
      (message "No previous variable." (beep))
    (goto-char (car nice-variable-stack))
    (setq nice-variable-stack (cdr nice-variable-stack))
    (nice-locate)))
  
(defun nice-find-variable ()
  "Locate a variable definition."
  (interactive)
  (let (point eol bol var found input)
    (setq point (point))
    (save-excursion
      (beginning-of-line)
      (setq bol (point))
      (goto-char point)
      (if (and (nice-search-backward "[^0-9]" bol)
               (looking-at ".\\([0-9]+\\)"))
          (progn
            (setq var (match-string-no-properties 1))
            (goto-char (point-min))
            (if (nice-search-forward (concat "^ " var ":.* = ") (point-max))
                (setq found (match-beginning 0))
              (progn
                (goto-char (point-min))
                (setq input (concat "^inputs {[^}]* " var " "))
                (if (nice-search-forward input (point-max))
                    (progn
                      (message "Input variable." (beep))
                      (setq input t))))))))
    (if (not found)
        (if (not input)
            (message "Definition not found." (beep)))
      (progn
        (setq nice-variable-stack (cons point nice-variable-stack))
        (goto-char found)
        (nice-locate)))))

(defun nice-comment-region-or-line ()
   "Comment the region, or the line if the mark is not active."
   (interactive)
   (if mark-active
       (nice-comment-region (min (point) (mark))
			  (max (point) (mark)))
     (nice-comment-region
      (save-excursion (beginning-of-line) (point))
      (save-excursion (end-of-line) (point)))))

(defun nice-uncomment-region-or-line ()
   "Uncomment the region, or the line if the mark is not active."
   (interactive)
   (if mark-active
       (nice-uncomment-region (min (point) (mark))
			    (max (point) (mark)))
     (nice-uncomment-region
      (save-excursion (beginning-of-line) (point))
      (save-excursion (end-of-line) (point)))))

(defun nice-comment-region (start end)
  (save-excursion
    (goto-char end)
    (setq end (point-marker))
    (save-restriction
      (narrow-to-region
       (progn (goto-char start) (beginning-of-line) (point))
       (progn (goto-char end) (or (bolp) (forward-line 1)) (point)))
      (goto-char (point-min))
      (while (not (eobp))
	(insert "//")
	(forward-line 1))))
  (indent-region start end nil))

(defun nice-uncomment-region (start end)
  (save-excursion
    (goto-char end)
    (setq end (point-marker))
    (save-restriction
      (narrow-to-region
       (progn (goto-char start) (beginning-of-line) (point))
       (progn (goto-char end) (forward-line 1) (point)))
      (goto-char (point-min))
      (let ((comment-regexp "\\s-*//"))
	(while (not (eobp))
	  (if (looking-at comment-regexp)
	      (delete-region (match-beginning 0) (match-end 0)))
	  (forward-line 1)))))
  (indent-region start end nil))

(defun nice-fontify-buffer ()
  "Fontifies the current Nice buffer."
  (interactive)
  (let ((modified (buffer-modified-p)))
    (remove-text-properties (point-min) (point-max) '(mouse-face))
    (font-lock-fontify-buffer)
    (set-buffer-modified-p modified)))

(defun nice-html-link (beg end)
  (let ((link (get-text-property beg 'nice-link)))
    (when link
      (let* ((job (nice-find-object (car link) (cdr link)))
             (kind (and job (nth 0 job)))
             (opackage (and job (nth 1 job)))
             (oname (and job (nth 2 job)))
             (opath (and job (nth 3 job))))
        (cond
         ((eq kind 'package)
          (concat "package."
                  (if opackage (concat opackage ".") "")
                  oname ".html"))
         ((eq kind 'file)
          (concat (if opackage (concat opackage ".") "")
                  oname nice-extension ".html"))
         (t "undefined.html"))))))

(defun nice-export (latex)
  (let* ((buf (if latex "*nice-latex-listing*" "*nice-html-listing*"))
         (file (concat nice-export-directory
                       "/" (concat (file-name-nondirectory (buffer-file-name))
                                   (if latex ".tex" ".html")))))
    (save-excursion
      (if (get-buffer buf)
          (kill-buffer buf))
      (if latex (nice-latex-listing) (nice-html-listing))
      (set-buffer buf)
      (write-file file nil)
      (set-file-modes file 420)
      (kill-buffer nil))))

(defun nice-html-export ()
  (interactive)
  (nice-export nil))
  
(defun nice-latex-export ()
  (interactive)
  (nice-export t))
  
(defun nice-export-package ()
  (interactive)
  (nice-export))

(defun nice-export-all-packages-in-dir (dir prefix)
  (let ((files (directory-files dir t nil nil)))
    (while files
      (let ((file (car files)))
        ;; Source file
        (when (nice-file-p file)
          (save-excursion
            (set-buffer (find-file-noselect file))
            (nice-export)))
        
        ;; Sub-package
        (when (nice-package-p file)
          (let* ((name (file-name-nondirectory file))
                 (package (if prefix (concat prefix "." name) name)))
            (nice-display-object 'package package t)
            (nice-export-all-packages-in-dir (car files) package))))
      (setq files (cdr files)))))
  
(defun nice-export-all-packages ()
  (interactive)
  (let ((path (cons "." (split-string (or (getenv "JAZZPATH") ".")
                                      nice-path-separator)))
        packages)
    (while path
      (nice-export-all-packages-in-dir (car path) nil)
      (setq path (cdr path)))))
                                 
(defun nice-export-output ()
  (interactive)
  (let* ((output (buffer-string))
         (name nice-output-name)
         (html "*nice-html-listing*")
         (file (concat nice-output-directory
                       "/" (concat nice-output-name ".out.html"))))
    (save-excursion
      (set-buffer (get-buffer-create html))
      (delete-region (point-min) (point-max))
      (insert "<HTML>\n<HEAD><TITLE>Output of &quot;")
      (insert name)
      (insert "&quot;</TITLE></HEAD>\n<BODY BGCOLOR=\"#ffffff\">\n<PRE>\n")
      (insert output)
      (insert "</PRE>\n</BODY>\n</HTML>")
      (write-file file nil)
      (set-file-modes file 420)
      (kill-buffer nil))))

(defun nice-html-listing ()
  "Formats current buffer in HTML"
  (interactive)
  (when (buffer-file-name) (nice-fontify-buffer))
  (let ((temp-buffer-show-function '(lambda (buffer))))
    (with-output-to-temp-buffer "*nice-html-listing*"
      (princ "<HTML><BODY BGCOLOR=\"#ffffff\"><PRE>\n")
      (let ((close nil)
            (beg 1)
            (end 0)
            face spec)
        (while (< end (point-max))
          (setq end (or (next-property-change beg) (point-max)))
          
          (let* ((face (get-text-property beg 'face))
                 (link (nice-html-link beg end))
                 (entry (or (and face
                                 (assoc face nice-html-faces-alist))
                            (and link
                                 (cons
                                  t
                                  (cons
                                   (concat
                                    "<A HREF=\"" link
                                    "\"><FONT COLOR=\"#000000\">")
                                   "</A></FONT>")))))
                 (spec (and entry (cdr entry))))
            (nice-html-print-segment close (and spec (car spec)) beg end)
            (setq close (and spec (cdr spec)))
            (setq beg end)))
        (if close (princ close)))
      (princ "</PRE></BODY></HTML>"))))

(defvar nice-html-faces-alist
  '((nice-keyword-face     . ("<FONT COLOR=\"#0000ee\">" . "</FONT>"))
    (nice-constant-face    . ("<FONT COLOR=\"#8080ff\">" . "</FONT>"))
    (nice-declaration-face . ("<FONT COLOR=\"#cd0000\">" . "</FONT>"))
    (nice-link-face        . ("<FONT COLOR=\"#000000\"><U>" . "</U></FONT>"))
    (nice-link-declaration-face
     . ("<FONT COLOR=\"#cd0000\"><U>" . "</U></FONT>"))
    (nice-comment-face     . ("<FONT COLOR=\"#008b00\">" . "</FONT>"))
    (nice-type-face        . ("<FONT COLOR=\"#b7860b\">" . "</FONT>"))
    (nice-string-face      . ("<FONT COLOR=\"#a020f0\">" . "</FONT>"))))

(defun nice-html-print-segment (close open beg end)
  (let (char)
    (if close (princ close))
    (if open  (princ open))
    (while (< beg end)
      (setq char (char-after beg))
      (cond
       ((= char ?<) (princ "&lt;"))
       ((= char ?>) (princ "&gt;"))
       ((= char ?&) (princ "&amp;"))
       ((= char ?\") (princ "&quot;"))
       ((= char ?\t) (princ "  "))
       (t (write-char char)))
      (setq beg (1+ beg)))))

(defun nice-latex-listing ()
  "Formats current buffer in LaTeX"
  (interactive)
  (when (buffer-file-name) (nice-fontify-buffer))
  (let ((temp-buffer-show-function '(lambda (buffer))))
    (with-output-to-temp-buffer "*nice-latex-listing*"
      (princ "\\begingroup")
      (princ "\\def\\color#1#2#3{\\special{ps: #1 #2 #3 setrgbcolor}}")
      (princ "\\def\\{{\\char`\\{}")
      (princ "\\def\\}{\\char`\\}}")
      (princ "\\def\\\\{\\char`\\\\}")
      (princ "\\begin{alltt}\n\n")
      (let ((beg 1)
	    (end 0)
	    face spec)
	(while (< end (point-max))
	  (setq end (or (next-property-change beg) (point-max)))
	  
	  (let* ((face (get-text-property beg 'face))
		 (color (or (and face
				 (cdr (assoc face nice-latex-color-alist)))
			    "#000000")))
	    (nice-latex-print-segment color beg end)
	    (setq beg end))))
      (princ "\\end{alltt}")
      (princ "\\endgroup"))))

(defvar nice-latex-color-alist
  '((nice-keyword-face     . "#0000ee")
    (nice-constant-face    . "#8080ff")
    (nice-declaration-face . "#cd0000")
    (nice-link-face        . "#000000")
    (nice-link-declaration-face . "#cd0000")
    (nice-comment-face     . "#008b00")
    (nice-type-face        . "#b7860b")
    (nice-string-face      . "#a020f0")))

(defun nice-color (color i)
  (/ (string-to-int (substring color (+ 1 (* 2 i)) (+ 3 (* 2 i))) 16) 255.0))
  
(defun nice-latex-print-segment (color beg end)
  (let ((char))
    (princ "\\color{")
    (princ (nice-color color 0))
    (princ "}{")
    (princ (nice-color color 1))
    (princ "}{")
    (princ (nice-color color 2))
    (princ "}")
    (while (< beg end)
      (setq char (char-after beg))
      (cond
       ((= char ?{) (princ "\\{"))
       ((= char ?}) (princ "\\}"))
       ((= char ?\\) (princ "\\\\"))
;       ((= char ?\n)
;        (princ "\n")
;        (save-excursion
;          (goto-char (1+ beg))
;          (let ((count (current-indentation)))
;            (while (> count 0)
;              (princ " ")
;              (setq count (1- count))))
;          (if (nice-search-forward "[^ 	]" (point-max))
;              (setq beg (- (point) 2)))))
       (t (write-char char)))
      (setq beg (1+ beg)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;;                          Internal functions/variables
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; dir must end with /
(defun nice-start-process (dir cmd)
  (message "%s//%s" dir cmd)
  (let (process
        (default-directory dir))
    (setq process (apply #'start-process "nice" "*Nice*" (car cmd) (cdr cmd)))
    (set-process-filter process 'nice-process-filter)
    (set-process-sentinel process 'nice-process-sentinel)
    process))
 
(defvar nice-compiling nil)
(or (assq 'nice-compiling minor-mode-alist)
    (setq minor-mode-alist (cons '(nice-compiling " Compiling")
                                 minor-mode-alist)))

(defun nice-search-forward (regexp limit)
  (let ((case-fold-search nil))
    (re-search-forward regexp limit t)))

(defun nice-search-backward (regexp limit)
  (let ((case-fold-search nil))
    (re-search-backward regexp limit t)))

;;; get rid of ^M on NT
(defun clean-insert(s)
  (if (eq window-system 'w32)
      (let ((max))
        (save-excursion
          (insert s)
          (setq max (point)))
        (while (nice-search-forward (char-to-string 13) max)
          (goto-char (match-beginning 0))
          (delete-char 1)
          (setq max (- max 1)))
        (goto-char max))
    (insert s)))

(defun nice-process-filter (process string)
  (save-excursion
    (set-buffer (process-buffer process))
    (goto-char (point-max))
    (save-excursion
      (clean-insert string))
    (beginning-of-line)
    (while (nice-search-forward "%[0-9\"].*:.*\n" (point-max))
      (save-excursion
        (goto-char (match-beginning 0))
        (indent-to (+ (- (frame-width) (match-end 0)) (match-beginning 0)))))))

(defun nice-process-sentinel (process change)
  (cond
   ((string-equal change "finished\n")
    (setq nice-compiling nil))
   ((string-match "exited abnormally with code 2" change)
    (setq nice-compiling nil)
    (beep)
    (message "Error"))
   ((string-match "exited abnormally with code" change)
    (setq nice-compiling nil)
    (beep)
    (message "Compiler bug" change)))
  (save-excursion
    (set-buffer (process-buffer process))
    (toggle-read-only 1)))

(defun nice-match-to-int (n)
  (string-to-int (match-string-no-properties n)))

(defun nice-line-char-to-pos (buffer l c)
  (save-excursion
    (set-buffer buffer)
    (goto-line l)
    (beginning-of-line)
    (forward-char (1- c))
    (point)))

(defun nice-point-on-current-line (point)
  (save-excursion
    (let ((bol (progn (beginning-of-line) (point))))
      (goto-char point)
      (beginning-of-line)
      (= bol (point)))))
  
(defun nice-select-file (name other l1 c1 l2 c2)
  (if (string-match "^[0-9]+$" name)
      (save-excursion
        (set-buffer "*Nice*")
        (goto-char (point-max))
        (if (nice-search-backward
             (concat name
                     ": %file:[ ]*\"\\([^\"]+\\)\" \"\\([^\"]+\\)\"")
             (point-min))
            (setq name (concat (match-string-no-properties 1) "/"
                               (match-string-no-properties 2))))))
  (if (not (file-name-absolute-p name))
      (setq name (concat nice-directory name)))
  (let ((buffer (and (file-exists-p name)
                     (find-file-noselect name))))
    (if (not buffer)
        (message "Cannot find file \"%s\"" name (beep))
      (setq c1 (nice-line-char-to-pos buffer l1 c1))
      (setq c2 (1+ (nice-line-char-to-pos buffer l2 c2)))
      (if other
          (pop-to-buffer buffer)
        (switch-to-buffer buffer))
      (if (not (get-buffer-window buffer))
          (message "Cannot select a buffer for \"%s\"" name)
        (nice-select (get-buffer-window buffer) c1 c2)))))

(defun nice-no-more-errors ()
  (beep)
  (message "No more errors."))

(defun nice-locate ()
  "Display the location indicated by the pragma at the end of the line."
  (interactive)
  (let ((window (selected-window))
        bol loc file l1 c1 l2 c2)
    (save-excursion
      (beginning-of-line)
      (setq bol (point))
      (end-of-line)
      (cond
       ((nice-search-backward nice-file-location-regexp bol)
        (nice-select-file
         (match-string-no-properties 1)
         t
         (nice-match-to-int 2)
         (nice-match-to-int 3)
         (nice-match-to-int 4)
         (nice-match-to-int 5))
        (select-window window))
       ((nice-search-backward "%\\([0-9]+\\)%" bol)
        (save-excursion
          (setq loc (concat "[^0-9]"
                            (match-string-no-properties 1)
                            ": %loc:[ ]*"
                            nice-loc-regexp))
          (goto-char (point-max))
          (if (nice-search-backward loc (point-min))
              (let ((file (match-string-no-properties 1))
                    (l1 (nice-match-to-int 2))
                    (c1 (nice-match-to-int 3))
                    (l2 (nice-match-to-int 4))
                    (c2 (nice-match-to-int 5)))
                (nice-select-file file t l1 c1 l2 c2)
                (select-window window)))))))))

(provide 'nice-mode)
