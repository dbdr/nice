;; Load this file from your .emacs with:
;;   (load "/usr/local/share/emacs/site-lisp/nice/nice-startup.el")

(setq load-path (cons "/usr/local/share/emacs/site-lisp/nice" load-path))

(setq auto-mode-alist (cons '("\\.nicei?$" . nice-mode) auto-mode-alist))
(autoload 'nice-mode "nice-mode" "Major mode for editing Nice code." t)
