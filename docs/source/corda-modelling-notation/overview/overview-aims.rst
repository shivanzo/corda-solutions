--------------------------------
Aims of Corda Modelling Notation
--------------------------------

In summary, Corda Modelling Notation should:

  1.	Allow corda designs to be documented in an accurate, clear and concise way to aid common understanding and reasoning over the design.

  2.	Describe and enable reasoning about key aspects of CorDapp design:

    *	Shared data and state
    *	Shared processing logic
    *	Permissioning (via digital signatures)
    *	Privacy (Visibility of data to different participants)
    *	Security



  3.  Draw a clear distinction between Ledger Layer and Orchestration Layer functionality.

  4.	Cope with increase complexity, importantly the modelling notation must not scale in complexity faster than the underlying application that the Model is representing.

  5.	Degrade gracefully and accurately. Not all aspects of the notations should be compulsory, but where details are left off the remaining diagram should remain accurate and self consistent for the level of detail chosen.

  6.	Minimised new modelling techniques by reusing and extending existing architecture and design techniques.

  7.	Allows standardised representation of reusable design patterns, including techniques for managing complexity.
