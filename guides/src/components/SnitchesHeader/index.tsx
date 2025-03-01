import React, { useEffect, useRef } from 'react';
import styles from './styles.module.css';

interface SnitchesHeaderProps {
  className?: string;
}

const SnitchesHeader: React.FC<SnitchesHeaderProps> = ({ className }) => {
  const topTextRef = useRef<HTMLHeadingElement>(null);
  const bottomTextRef = useRef<HTMLHeadingElement>(null);
  const containerRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const calculateProportionalFontSize = () => {
      if (topTextRef.current && bottomTextRef.current) {
        // Constants - using fixed-width font, so we can calculate based on character count
        const topText = 'SNITCHES';
        const bottomText = 'ON YOUR API';

        // Get the ratio of character counts (with fixed-width font, this corresponds to width)
        const charRatio = bottomText.length / topText.length;

        // Get the base font size of the top text
        const topFontSize = parseFloat(window.getComputedStyle(topTextRef.current).fontSize);

        // Calculate the bottom text font size to make it the same width
        // In a monospace font, we scale inversely proportional to character count
        const bottomFontSize = topFontSize / charRatio;

        // Apply the calculated font size
        bottomTextRef.current.style.fontSize = `${bottomFontSize}px`;
      }
    };

    // Run on mount and window resize
    calculateProportionalFontSize();
    window.addEventListener('resize', calculateProportionalFontSize);

    return () => {
      window.removeEventListener('resize', calculateProportionalFontSize);
    };
  }, []);

  return (
    <div className={`${styles.container} ${className || ''}`}>
      <div className={styles.bar}></div>
      <div className={styles.textContainer} ref={containerRef}>
        <h1 className={styles.titleTop} ref={topTextRef}>SNITCHES</h1>
        <h2 className={styles.titleBottom} ref={bottomTextRef}>ON YOUR API</h2>
      </div>
      <div className={styles.bar}></div>
    </div>
  );
};

export default SnitchesHeader;
