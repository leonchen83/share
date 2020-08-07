## scheme y-combinator 推导  
2014年05月06日 22:15:32 leon_a 阅读数：127  
```lisp
(define (part-fib self n)
	(if (= n 0) 1
	 (* n (self self (- n 1)))))

(part-fib part-fib 5)

(define (part-fib self) 
	(lambda (n) 
		(if (= n 0) 1 
			(* n ((self self) (- n 1))))))

((part-fib part-fib) 5)

(define (part-fib self) 
	(let ((f (self self))) (lambda (n) 
		(if (= n 0) 1 
			(* n (f (- n 1)))))))


(define (part-fib self) 
	((lambda (f) 
		(lambda (n) 
			(if (= n 0) 1 (* n (f (- n 1)))))) (self self)))

(define g (lambda (f) 
	(lambda (n) (if (= n 0) 1 (* n (f (- n 1)))))))

(define (part-fib self) 
	(g (self self)))

(define part-fib 
	(lambda (self) 
		(g (self self))))

(define fib 
	(let ((part-fib (lambda (self) 
		(g (self self))))) 
	(part-fib part-fib)))

(define fib 
	((lambda (part-fib) 
		(part-fib part-fib)) 
	(lambda (self) 
		(g (self self))))

(define (fib g)
	((lambda (part-fib) 
		(part-fib part-fib)) 
	(lambda (self) 
		(g (self self))))

;;lazy evaluation
(define y 
	(lambda (f) 
		((lambda (x) 
			(x x)) 
		(lambda (x) 
			(f (x x))))))

;;strict evaluation
(define y 
	(lambda (f) 
		((lambda (x) 
			(lambda (arg) ((x x) arg))) 
		(lambda (x) 
			(f (lambda (arg) ((x x) arg)))))))
```
