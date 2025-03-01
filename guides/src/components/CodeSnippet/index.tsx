import React from 'react';
import styles from './styles.module.css';

interface CodeSnippetProps {
  className?: string;
}

const CodeSnippet: React.FC<CodeSnippetProps> = ({ className }) => {
  return (
    <div className={styles.codesection}>
      <div className={styles.codecontainer}>
        <pre className={styles.codeblock}>
{`GET("in" / "everything") isHandledBy {
    "Snitch. You'll be".ok
}`}
        </pre>
      </div>
    </div>
  );
};

export default CodeSnippet;